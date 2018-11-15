package com.jiayou.fyg.myapplication;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import static java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * @author fyg
 * @date : 2018/9/9
 * @E-Mail ：2355245065@qq.com
 * @Wechat :fyg13522647431
 * @Tel : 13522647431
 * @desc :
 */
public class StateMachine {

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

    @IntDef(flag = true, value = {STATE_ONE, STATE_TWO, STATE_THREE, STATE_FOUR, STATE_FIVE, STATE_SIX,})
    @Retention(RetentionPolicy.SOURCE)
    public @interface STATE {
    }


    public void addState(@STATE final int flag) {
        updateWriteState(new Callback<Void>() {
            @Override
            public Void callback() {
                mState |= flag;
                return null;
            }
        });
    }

    public void removeState(@STATE final int flag) {

        updateWriteState(new Callback<Void>() {
            @Override
            public Void callback() {
                mState &= ~flag;
                return null;
            }
        });
    }

    public void clearState() {

        updateWriteState(new Callback<Void>() {
            @Override
            public Void callback() {
                mInitState = mState &= STATE_ZERO;
                return null;
            }
        });
    }


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

    public boolean compareState(final StateMachine state) {

        return updateReadState(new Callback<Boolean>() {
            @Override
            public Boolean callback() {
                return (mState ^ state.getCurrentState()) == 0;
            }
        });
    }


    private boolean compareState(@STATE final int state) {

        return updateReadState(new Callback<Boolean>() {
            @Override
            public Boolean callback() {
                return (mState ^ state) == 0;
            }
        });
    }

    public int getDiffState(final StateMachine  state) {

        return updateReadState(new Callback<Integer>() {
            @Override
            public Integer callback() {
                return mState ^ state.getCurrentState();
            }
        });
    }


    private int getDiffState(@STATE final int state) {

        return updateReadState(new Callback<Integer>() {
            @Override
            public Integer callback() {
                return mState ^ state;
            }
        });
    }

    public int getCurrentState() {
        return mState;
    }

    public int getState(@STATE final int state) {

        return updateReadState(new Callback<Integer>() {
            @Override
            public Integer callback() {
                return mState & state;
            }
        });
    }

    public String getStateWithBinary(@STATE final int state) {

        return updateReadState(new Callback<String>() {
            @Override
            public String callback() {
                return Integer.toBinaryString(getState(state));
            }
        });
    }


    public boolean hasState(@STATE final int state) {

        return updateReadState(new Callback<Boolean>() {
            @Override
            public Boolean callback() {
                return getState(state) == state;
            }
        });

    }


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

    private static interface Callback<T> {
        T callback();
    }


    public static StateMachine createStateMachine(@STATE int initState) {
        return new StateMachine(initState);
    }

    public static StateMachine createStateMachine() {
        return createStateMachine(STATE_ZERO);
    }


    private StateMachine(@STATE int initState) {
        mState = mInitState = initState;
    }

    private StateMachine() {
    }


    @Override
    public String toString() {
        return "InitState "+ Integer.toBinaryString(mInitState) + "   current State : " + Integer.toBinaryString(mState);
    }






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


    public static void testAddMethod(){

        StateMachine stateMachine = StateMachine.createStateMachine();
        System.out.println(stateMachine.toString());
        stateMachine.addState(STATE_ONE);
        stateMachine.addState(STATE_TWO);
        stateMachine.addState(STATE_THREE);
        stateMachine.addState(STATE_FOUR);
        stateMachine.addState(STATE_FIVE);
        stateMachine.addState(STATE_SIX);


        //query state
        System.out.println("stateMachine.hasState(STATE_ONE)  "+stateMachine.hasState(STATE_ONE));
        System.out.println("stateMachine.hasState(STATE_TWO)  "+stateMachine.hasState(STATE_TWO));
        System.out.println("stateMachine.hasState(STATE_THREE)  "+stateMachine.hasState(STATE_THREE));
        System.out.println("stateMachine.hasState(STATE_FOUR)  "+stateMachine.hasState(STATE_FOUR));
        System.out.println("stateMachine.hasState(STATE_FIVE)  "+stateMachine.hasState(STATE_FIVE));
        System.out.println("stateMachine.hasState(STATE_SIX)  "+stateMachine.hasState(STATE_SIX));

        System.out.println(stateMachine.toString());


    }



    public static void testDeleteMethod(){


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
        System.out.println(stateMachine.toString());



    }



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
        System.out.println(stateMachine.toString());

        //重置状态
        stateMachine.resetState();
        System.out.println(stateMachine.toString());



    }


    public static void testDiffMethod(){


        StateMachine stateMachine1 = StateMachine.createStateMachine(STATE_ONE|STATE_TWO|STATE_TWO|STATE_THREE|STATE_FOUR|STATE_FIVE|STATE_SIX);
        System.out.println("stateMachine1 "+stateMachine1.toString());
        StateMachine stateMachine2 = StateMachine.createStateMachine();
        System.out.println("stateMachine2 "+stateMachine2.toString());
        System.out.println("getDiffState  ： "+Integer.toBinaryString(stateMachine1.getDiffState(stateMachine2)));

    }

    public static void testCompareState(){
        StateMachine stateMachine1 = StateMachine.createStateMachine(STATE_TWO|STATE_THREE|STATE_FOUR|STATE_FIVE|STATE_SIX);
        System.out.println("stateMachine1 "+stateMachine1.toString());
        StateMachine stateMachine2 = StateMachine.createStateMachine();
        System.out.println("stateMachine2 "+stateMachine2.toString());

        System.out.println("compareState  ： "+stateMachine1.compareState(stateMachine2));


    }

}
