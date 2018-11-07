package com.pa.chen.asyncmsghandler;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.concurrent.LinkedBlockingQueue;

//创建一个线程循环，主线程向线程发送消息。
public class MainActivity extends Activity {
    public final static String className = "MainActivity";
    Looper thLooper;
    MyHandler myHandler;

    Method postSyncBarrier;

    boolean mQuit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyThread myThread = new MyThread();
        myThread.start();//启动线程
        //建立非主线程消息循环。
        thLooper = myThread.getLooper();
        myHandler = new MyHandler(thLooper);

        //消息队列插入一个同步栅栏，隐藏方法
        int token;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                postSyncBarrier = MessageQueue.class.getMethod("postSyncBarrier");
                token = (int) postSyncBarrier.invoke(thLooper.getQueue());
            } else {
                postSyncBarrier = Looper.class.getMethod("postSyncBarrier");
                token = (int) postSyncBarrier.invoke(thLooper);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //UI监听事件
        TextView mTextView = (TextView) findViewById(R.id.tv_send_async_msg);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送5个消息，4个同步消息，1个异步消息
                myHandler.sendEmptyMessageDelayed(1, 45000);
                myHandler.sendEmptyMessageDelayed(1, 15000);
                myHandler.sendEmptyMessageDelayed(1, 13000);
                myHandler.sendEmptyMessageDelayed(1, 11000);

                Message message = Message.obtain();
                message.what = 1;
                message.obj = "msg_Asynchronous";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    message.setAsynchronous(true);
                }
                myHandler.sendMessageDelayed(message, 25000);
            }
        });
        //创建一个阻塞队列用于线程消息通信。
//        final LinkedBlockingQueue<String> linkedBlockingQueue = new LinkedBlockingQueue<>();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    while (!mQuit) {
//                        String clear = linkedBlockingQueue.take();
//                        Log.d(Constants.TAG_1, clear);
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//        //UI监听事件
//        TextView mTextView = (TextView) findViewById(R.id.tv_send_async_msg);
//        mTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    linkedBlockingQueue.put("clear");
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // 处理线程消息
            Log.d(Constants.TAG_1, Thread.currentThread().getName());
            switch (msg.what) {
                case 1:
                    if (msg.obj == null) {
                        Log.d(Constants.TAG_1, "empty msg");
                    } else {
                        Log.d(Constants.TAG_1, msg.obj.toString());
                    }
                    break;
            }

        }
    }

    private Method getHideMethod(String name) {
        Method method = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                method = MessageQueue.class.getMethod(name);
            } else {
                method = Looper.class.getMethod(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return method;
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thLooper.quit();
        mQuit = true;
    }
}
