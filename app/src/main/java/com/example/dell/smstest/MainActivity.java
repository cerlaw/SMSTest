package com.example.dell.smstest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView sender;

    private TextView content;

    private EditText to;

    private EditText smsInput;

    private Button send;

    private IntentFilter intentfilter;

    private MessageReceiver messageReceiver;

    private IntentFilter sendFilter;

    private SendStatusReceiver sendStatusReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        to = (EditText) findViewById(R.id.to);
        smsInput = (EditText) findViewById(R.id.mag_input);
        send = (Button) findViewById(R.id.send);
        sender = (TextView) findViewById(R.id.sender);
        content = (TextView) findViewById(R.id.content);
        intentfilter = new IntentFilter();
        intentfilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentfilter.setPriority(100);//提高messageReceiver的优先级
        sendFilter = new IntentFilter();
        sendFilter.addAction("SENT_SMS_ACTION");
        sendStatusReceiver = new SendStatusReceiver();
        registerReceiver(sendStatusReceiver,sendFilter);
        messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver,intentfilter);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmsManager smsManager = SmsManager.getDefault();
                Intent intent = new Intent("SENT_SMS_ACTION");
                PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this,
                        0,intent,0);
                smsManager.sendTextMessage(to.getText().toString(),null,
                        smsInput.getText().toString(),pi,null);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(messageReceiver);
        unregisterReceiver(sendStatusReceiver);
    }

    class MessageReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            abortBroadcast();//中止广播的继续传递，从而实现拦截,没有实现
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus");//获取短信信息
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for(int i = 0; i < messages.length; i++){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = intent.getStringExtra("format");
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i],format);
                }else{
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                String address = messages[i].getOriginatingAddress();//获取发送号码
                String fullmessages = "";
                for (SmsMessage message: messages) {
                    fullmessages += message.getMessageBody();//获取短信内容
                }
                sender.setText(address);
                content.setText(fullmessages);
            }
        }
    }

    class SendStatusReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(getResultCode() == RESULT_OK){
                Toast.makeText(context,"Send Succeeded",Toast.LENGTH_LONG).
                        show();
            }else {
                Toast.makeText(context,"Send failed",Toast.LENGTH_LONG).show();
            }
        }
    }
}
