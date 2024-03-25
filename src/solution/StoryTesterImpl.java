package solution;

import org.junit.ComparisonFailure;
import provided.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;


public class StoryTesterImpl implements StoryTester {

    private Object objectBackup;

    String firstFailedSentence;
    String expected;
    String result;
    int numFails;

    static StoryTestExceptionImpl storyTestException;


    /** Creates and returns a new instance of testClass **/
    private static Object createTestInstance(Class<?> testClass) throws Exception {

        try {
            // TODO: Try constructing a new instance using the default constructor of testClass -- should be done
            Constructor<?> newInstance = testClass.getConstructor();
            newInstance.setAccessible(true);
            return newInstance.newInstance();
        } catch (Exception e) {
            // TODO: Inner classes case; Need to first create an instance of the enclosing class -- should be done
            Object enclosingInstance = createTestInstance(testClass.getEnclosingClass());
            Constructor<?> constructor = testClass.getConstructor(testClass.getEnclosingClass());

            constructor.setAccessible(true);
            Object newInnerInstance = constructor.newInstance(enclosingInstance);

            return newInnerInstance;
        }
    }

    /** Returns true if c has a copy constructor, or false if it doesn't **/
    private boolean copyConstructorExists(Class<?> c){
        try {
            c.getDeclaredConstructor(c);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /** Assigns into objectBackup a backup of obj.
    /** See homework's pdf for more details on backing up and restoring **/
    private void backUpInstance(Object obj) throws Exception {
        Object res = createTestInstance(obj.getClass());
        Field[] fieldsArr = obj.getClass().getDeclaredFields();
        for(Field field : fieldsArr){
            field.setAccessible(true);
            Object fieldObject = field.get(obj);
            if (fieldObject == null) {
                field.set(res, null);
                continue;
            }
            Class<?> fieldClass = fieldObject.getClass();

            if(fieldObject instanceof Cloneable){
                // TODO: Case1 - Object in field is cloneable
                Method cloneMethod = fieldClass.getMethod("clone");
                field.set(res, cloneMethod.invoke(fieldObject));

            }
            else if(copyConstructorExists(fieldClass)){
                // TODO: Case2 - Object in field is not cloneable but copy constructor exists
                Constructor<?> constructor = fieldClass.getDeclaredConstructor(fieldClass);
                constructor.setAccessible(true);
                field.set(res, constructor.newInstance(fieldObject));

            }
            else{
                // TODO: Case3 - Object in field is not cloneable and copy constructor does not exist
                field.set(res, fieldObject);
            }
        }
        this.objectBackup = res;
    }

    /** Assigns into obj's fields the values in objectBackup fields.
    /** See homework's pdf for more details on backing up and restoring **/
    private void restoreInstance(Object obj) throws Exception{
        Field[] classFields = obj.getClass().getDeclaredFields();
        for(Field field : classFields) {
            // TODO: Complete.
            field.setAccessible(true);
            field.set(obj, field.get(objectBackup));
        }
    }

    /** Returns the matching annotation class according to annotationName (Given, When or Then) **/
    private static Class<? extends Annotation> GetAnnotationClass(String annotationName) throws WordNotFoundException{
        switch (annotationName) {
            // TODO: Return matching annotation class
            case "Given":
                return Given.class;
            case "When":
                return When.class;
            case "Then":
                return Then.class;
        }
        throw new WordNotFoundException();
    }

    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws Exception {
        storyTestException = new StoryTestExceptionImpl();
        if ((story == null) || testClass == null) throw new IllegalArgumentException();

        this.numFails = 0;
        Object testInstance = createTestInstance(testClass);


        boolean flag = false;

        for (String sentence : story.split("\n")) {
            boolean methodFound = false;
            String[] words = sentence.split(" ", 2);

            String annotationName = words[0];

            Class<? extends Annotation> annotationClass = GetAnnotationClass(annotationName);

            String sentenceSub = words[1].substring(0, words[1].lastIndexOf(' ')); // Sentence without the parameter and annotation
            String parameter = sentence.substring(sentence.lastIndexOf(' ') + 1);

            Method method = getMethod(testClass, sentenceSub, annotationClass);

            if (annotationName.equals("When") && !flag) {
                backUpInstance(testInstance);
                flag = true;
            }
            if (annotationName.equals("Then")) {
                flag = false;
            }
            if (method != null) {
                method.setAccessible(true);
                try {
                    // check if a method takes parameter as string or integer
                    if (method.getParameterTypes()[0].equals(String.class)) {
                        method.invoke(testInstance, parameter);
                    } else {
                        method.invoke(testInstance, Integer.parseInt(parameter));
                    }
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof ComparisonFailure) {
                        this.numFails++;
                        if (this.numFails == 1) {
                            storyTestException.line = sentence;
                            storyTestException.expected = ((ComparisonFailure) e.getCause()).getExpected();
                            storyTestException.actual = ((ComparisonFailure) e.getCause()).getActual();
                            restoreInstance(testInstance);
                        }
                    }
                }
            }
        }
        if (this.numFails > 0) {
            storyTestException.setNumFail(this.numFails);
            throw storyTestException;
        }
    }

    private Method getMethod(Class<?> testClass, String sentence, Class<? extends Annotation> annotationClass) throws GivenNotFoundException, ThenNotFoundException, WhenNotFoundException {
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                return method;
            }
        }
        Class<?> superclass = testClass.getSuperclass();
        if (superclass != null) {
            return getMethod(superclass, sentence, annotationClass);
        } else {
            switch (annotationClass.getSimpleName()) {
                case "Given":
                    throw new GivenNotFoundException();
                case "Then":
                    throw new ThenNotFoundException();
                case "When":
                    throw new WhenNotFoundException();
                default:
                    return null;
            }
        }
}
    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        if ((story == null) || testClass == null) throw new IllegalArgumentException();
        try{
            testOnInheritanceTree(story, testClass);
        } catch (GivenNotFoundException e) {
            //iterate over the nested classes
            for (Class<?> innerClass : testClass.getDeclaredClasses()) {
                testOnNestedClasses(story, innerClass);
            }
        }
    }
}
