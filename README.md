# StateMachine
封装位运算符，使用更加方便



```java

/**
 * @author fyg
 * @date : 2018/9/9
 * @E-Mail ：2355245065@qq.com
 * @Wechat :fyg13522647431
 * @Tel : 13522647431
 * @desc :
 */
public class StateMachine {

    /**
     * 使用读写锁，可用于多线程中，
     * 注意 锁升级，会产生死锁，同一个线程在没有释放读锁的情况下去申请写锁是不成立的，
     * 不然会产生死锁
     */
    private final ReadWriteLock mLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock mWriteLock = (WriteLock) mLock.writeLock();
    private final ReentrantReadWriteLock.ReadLock mReadLock = (ReadLock) mLock.readLock();

    /**
     * 0
     */
    private static final int STATE_ZERO      = 0x00000000;
    /**
     * 1
     */
    private static final int STATE_ONE       = 0x00000001;
    /**
     * 10
     */
    private static final int STATE_TWO       = 0x00000002;
    /**
     * 100
     */
    private static final int STATE_THREE     = 0x00000004;
    /**
     * 1000
     */
    private static final int STATE_FOUR      = 0x00000008;

    /**
     * 10000
     */
    private static final int STATE_FIVE      = 0x00000010;
    /**
     * 100000
     */
    private static final int STATE_SIX       = 0x00000020;


    private int mState, mInitState;

    private static final long serialVersionUID = 1L;

    /**
     * 限定状态范围
     */
    @IntDef(flag = true, value = {STATE_ONE, STATE_TWO, STATE_THREE, STATE_FOUR, STATE_FIVE, STATE_SIX,})
    @Retention(RetentionPolicy.SOURCE)
    public @interface STATE {
    }


    /**
     * 通过 | 增加状态
     * @param flag
     */
    public void addState(@STATE final int flag) {
        updateWriteState(new Callback<Void>() {
            @Override
            public Void callback() {
                mState |= flag;
                return null;
            }
        });
    }

    /**
     * 通过 &(~flag) 移除状态
     * @param flag
     */
    public void removeState(@STATE final int flag) {

        updateWriteState(new Callback<Void>() {
            @Override
            public Void callback() {
                mState &= ~flag;
                return null;
            }
        });
    }

    /**
     * 清除所有状态包括 初始状态和 当前状态
     */
    public void clearState() {

        updateWriteState(new Callback<Void>() {
            @Override
            public Void callback() {
                mInitState = mState &= STATE_ZERO;
                return null;
            }
        });
    }

    /**
     * 重置当前状态为初始状态
     */
    public void resetState() {

        updateWriteState(new Callback<Void>() {
            @Override
            public Void callback() {
                mState &= STATE_ZERO;
                mState |= mInitState;
                return null;
            }
        });
    }

    /**
     * 比较两状态机 当前状态是否相同
     * @param state
     * @return
     */
    public boolean compareState(final StateMachine state) {

        return updateReadState(new Callback<Boolean>() {
            @Override
            public Boolean callback() {
                return (mState ^ state.getCurrentState()) == 0;
            }
        });
    }


    /**
     * * 比较当前状态是否相同
     * @param state
     * @return
     */
    private boolean compareState(@STATE final int state) {

        return updateReadState(new Callback<Boolean>() {
            @Override
            public Boolean callback() {
                return (mState ^ state) == 0;
            }
        });
    }

    /**
     * 获取两状态机 当前不同的状态
     * @param state
     * @return
     */
    public int getDiffState(final StateMachine  state) {

        return updateReadState(new Callback<Integer>() {
            @Override
            public Integer callback() {
                return mState ^ state.getCurrentState();
            }
        });
    }

    /**
     * 获取当前不同的状态
     * @param state
     * @return
     */
    private int getDiffState(@STATE final int state) {

        return updateReadState(new Callback<Integer>() {
            @Override
            public Integer callback() {
                return mState ^ state;
            }
        });
    }

    /**
     * 获取状态机当前状态
     * @return
     */
    public int getCurrentState() {
        return mState;
    }

    /**
     * 获取状态机某二进制位的状态
     * @param state
     * @return
     */
    public int getState(@STATE final int state) {

        return updateReadState(new Callback<Integer>() {
            @Override
            public Integer callback() {
                return mState & state;
            }
        });
    }

    /**
     * 返回当前状态的二进制字符串形式
     * @param state
     * @return
     */
    public String getStateWithBinary(@STATE final int state) {

        return updateReadState(new Callback<String>() {
            @Override
            public String callback() {
                return Integer.toBinaryString(getState(state));
            }
        });
    }


    /**
     * 查询某二进制位的状态
     * @param state
     * @return
     */
    public boolean hasState(@STATE final int state) {

        return updateReadState(new Callback<Boolean>() {
            @Override
            public Boolean callback() {
                return getState(state) == state;
            }
        });

    }


    /**
     * 通过写锁，更新操作
     * @param callback
     * @param <T>
     * @return
     */
    private <T> T updateWriteState(Callback<T> callback) {

        mWriteLock.lock();
        try {
            if (callback != null) {
                return callback.callback();
            }
        } finally {
            mWriteLock.unlock();
        }
        return null;
    }

    /**
     * * 通过读锁，查询操作
     * @param callback
     * @param <T>
     * @return
     */
    private <T> T updateReadState(Callback<T> callback) {

        mReadLock.lock();
        try {
            if (callback != null) {
                return callback.callback();
            }
        } finally {
            mReadLock.unlock();
        }
        return null;
    }

    /**
     * 对扩展开放
     * @param <T>
     */
    private static interface Callback<T> {
        T callback();
    }


    /**
     * 静态方法创建实例对象
     * @param initState
     * @return
     */
    public static StateMachine createStateMachine(@STATE int initState) {
        return new StateMachine(initState);
    }

    public static StateMachine createStateMachine() {
        return createStateMachine(STATE_ZERO);
    }


    /**
     * 有参构造
     * @param initState
     */
    private StateMachine(@STATE int initState) {
        mState = mInitState = initState;
    }

    /**
     * 构建私有化，提供默认构造，允许反射创建
     */
    private StateMachine() {
    }


    @Override
    public String toString() {
        return "InitState "+ Integer.toBinaryString(mInitState) + "   current State : " + Integer.toBinaryString(mState);
    }
    
    public  static <T> void println(T t){
        System.out.println(t);
    }
}
```





###1.增加状态方法

```java

	/**
     * 测试增加状态方法
     */
    public static void testAddMethod(){

        StateMachine stateMachine = StateMachine.createStateMachine();
        //调用 toString方法获取状态
        System.out.println(stateMachine.toString());
        stateMachine.addState(STATE_ONE);
        stateMachine.addState(STATE_TWO);
        stateMachine.addState(STATE_THREE);
        stateMachine.addState(STATE_FOUR);
        stateMachine.addState(STATE_FIVE);
        stateMachine.addState(STATE_SIX);

        //query state
        println("stateMachine.hasState(STATE_ONE)  "+stateMachine.hasState(STATE_ONE));
        println("stateMachine.hasState(STATE_TWO)  "+stateMachine.hasState(STATE_TWO));
        println("stateMachine.hasState(STATE_THREE)  "+stateMachine.hasState(STATE_THREE));
        println("stateMachine.hasState(STATE_FOUR)  "+stateMachine.hasState(STATE_FOUR));
        println("stateMachine.hasState(STATE_FIVE)  "+stateMachine.hasState(STATE_FIVE));
        println("stateMachine.hasState(STATE_SIX)  "+stateMachine.hasState(STATE_SIX));
        //调用 toString方法获取状态
        println(stateMachine.toString());

    }
```

执行结果

```
InitState 0   current State : 0
stateMachine.hasState(STATE_ONE)  true
stateMachine.hasState(STATE_TWO)  true
stateMachine.hasState(STATE_THREE)  true
stateMachine.hasState(STATE_FOUR)  true
stateMachine.hasState(STATE_FIVE)  true
stateMachine.hasState(STATE_SIX)  true
InitState 0   current State : 111111
```





### 2.移除状态方法

```java
/**
 * 测试移除状态
 */
public static void testDeleteMethod(){

    StateMachine stateMachine = StateMachine.createStateMachine(STATE_ONE|STATE_TWO|STATE_TWO|STATE_THREE|STATE_FOUR|STATE_FIVE|STATE_SIX);

    //print InitState  and current state
    println(stateMachine.toString());

    stateMachine.removeState(STATE_ONE);
    stateMachine.removeState(STATE_TWO);
    stateMachine.removeState(STATE_THREE);
    stateMachine.removeState(STATE_FOUR);
    stateMachine.removeState(STATE_FIVE);
    stateMachine.removeState(STATE_SIX);

    //print InitState  and current state
    println(stateMachine.toString());
    
}
```

执行结果

```
InitState 111111   current State : 111111
InitState 111111   current State : 0
```



### 3.重置状态方法

```java
/**
 * 重置状态方法，会重置为初始值
 */
public static void testResetMethod(){

    StateMachine stateMachine = StateMachine.createStateMachine(STATE_ONE|STATE_TWO|STATE_TWO|STATE_THREE|STATE_FOUR|STATE_FIVE|STATE_SIX);

    //print InitState  and current state
    System.out.println(stateMachine.toString());

    stateMachine.removeState(STATE_ONE);
    stateMachine.removeState(STATE_TWO);
    stateMachine.removeState(STATE_THREE);
    stateMachine.removeState(STATE_FOUR);
    stateMachine.removeState(STATE_FIVE);
    stateMachine.removeState(STATE_SIX);

    //print InitState  and current state
    println(stateMachine.toString());

    //重置状态
    stateMachine.resetState();
    println(stateMachine.toString());

}
```

执行结果

```
InitState 111111   current State : 111111
InitState 111111   current State : 0
InitState 111111   current State : 111111
```



### 4.以二进制形式获取状态机不同位

```java
/**
 * 状态机比较方法，列出，不同的位
 */
public static void testDiffMethod(){
    
    StateMachine stateMachine1 = StateMachine.createStateMachine(STATE_ONE|STATE_TWO|STATE_THREE|STATE_FOUR|STATE_FIVE|STATE_SIX);
    println("stateMachine1 "+stateMachine1.toString());
    StateMachine stateMachine2 = StateMachine.createStateMachine(STATE_ONE);
    println("stateMachine2 "+stateMachine2.toString());
    println("getDiffState  ： "+Integer.toBinaryString(stateMachine1.getDiffState(stateMachine2)));

}
```

执行结果

```java
stateMachine1 InitState 111111   current State : 111111
stateMachine2 InitState 1   current State : 1
getDiffState  ： 111110
```



### 5.比较方法

```java
/**
 * 比较方法
 */
public static void testCompareState(){
    StateMachine stateMachine1 = StateMachine.createStateMachine(STATE_TWO|STATE_THREE|STATE_FOUR|STATE_FIVE|STATE_SIX);
    println("stateMachine1 "+stateMachine1.toString());
    StateMachine stateMachine2 = StateMachine.createStateMachine();
    println("stateMachine2 "+stateMachine2.toString());

    println("compareState  ： "+stateMachine1.compareState(stateMachine2));
}
```

执行结果

```
stateMachine1 InitState 111110   current State : 111110
stateMachine2 InitState 0   current State : 0
compareState  ： false
```



####android studio 在当前类中右键 运行【Run StateMachine.main()】 及可进行验证



```java
public static void main(String[] args){

    System.out.println(" ---------      testAddMethod start     --------- ");

    testAddMethod();

    System.out.println(" ---------      testDeleteMethod start     --------- ");
    testDeleteMethod();

    System.out.println(" ---------      testResetMethod start     --------- ");
    testResetMethod();

    System.out.println(" ---------      testDiffMethod start     --------- ");
    testDiffMethod();

    System.out.println(" ---------      testCompareState start     --------- ");
    testCompareState();
}
```