package aop;

class Clazz implements Interface{

    @Override
    public  String hello(String args) {
        return "hello"+ args;
    }
}