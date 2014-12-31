/*  @author : naper
 *  @This app will print any web page or text file .
 *  @Compatible with all android devices .
 *  @You can't test it on an emualtor if your PC doesn't support bluetooth otherwise you'll get a null pointer exception
 */
package com.zj.printdemo;

import java.util.Set;

import com.zj.printdemo.R;

import android.content.Intent;
import com.zj.btsdk.BluetoothService;
import com.zj.btsdk.PrintPic;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import java.net.URL;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;
import java.io.File;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.net.ProtocolException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import android.util.Log;

public class PrintDemo extends Activity {
	Button btnSearch;
	Button btnSendDraw;
    Button btn;
	Button btnSend;
	Button btnClose;
    Button back;
    TextView textv;
	EditText edtContext;
	EditText edtPrint;
	private static final int REQUEST_ENABLE_BT = 2;
	BluetoothService mService = null;
	BluetoothDevice con_dev = null;
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private int conn_flag = 0;
	private ConnectPaireDev mConnPaireDev = null;
    public String URLPAGE = "http://vps90820.ovh.net/";
    public StringBuilder finalText;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		mService = new BluetoothService(this, mHandler);
        final WebView mywebview = (WebView) findViewById(R.id.webView);
        Button print_btn = (Button) findViewById(R.id.btnSend);
        ProgressBar loading = (ProgressBar) findViewById(R.id.progressBar);
        loading.setVisibility(View.VISIBLE);
        /* set finished loading event */
        mywebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                edtContext = (EditText) findViewById(R.id.txt_content);
                Button print_btn = (Button) findViewById(R.id.btnSend);
                Button btn_search = (Button) findViewById(R.id.btnSearch);
                TextView textv = (TextView) findViewById(R.id.editText);
                ProgressBar loading = (ProgressBar) findViewById(R.id.progressBar);
                readFile();
                loading.setVisibility(View.INVISIBLE);
                btn_search.setVisibility(View.VISIBLE);
                print_btn.setVisibility(View.VISIBLE); /*  print will be visible */
                print_btn.setText("Imprimer Page Web"); /* we set the text */
                if(checkUrl() == 1){
                    print_btn.setVisibility(View.VISIBLE); /*  print will be visible */
                    print_btn.setText("Imprimer Fichier text"); /* we set the text */
                }
                /* set textedit to webview url */
                textv.setText(mywebview.getUrl().toString());
            }
        });

		if( mService.isAvailable() == false ){
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
		}
		if( mService.isBTopen() == false)
		{
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
        openUrl(URLPAGE);
	}
    public void openUrl(String url){
        WebView mywebview = (WebView) findViewById(R.id.webView);
        mywebview.getSettings().setLoadsImagesAutomatically(true);
        mywebview.getSettings().setJavaScriptEnabled(true);
        mywebview.loadUrl(url);

        return ;
    }
    public int checkUrl(){
        WebView mywebview = (WebView) findViewById(R.id.webView);
        String Wvurl = mywebview.getUrl().toString();
        if(Wvurl.contains(".txt")){
            return 1;
        }else{
            return -1;
        }
    }
    public void readFile(){
        WebView mywebview = (WebView) findViewById(R.id.webView);
        URL u = null;
        try {
            u = new URL(mywebview.getUrl().toString());
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.connect();
            InputStream in = c.getInputStream();
            final ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            in.read(buffer); // Read from Buffer.
            bo.write(buffer); // Write Into Buffer.

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    edtContext = (EditText) findViewById(R.id.txt_content);
                    edtContext.setText(bo.toString());
                    try {
                        bo.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void onStart() {
    	super.onStart();
		try {
			btnSendDraw = (Button) this.findViewById(R.id.btn_test);
			btnSendDraw.setOnClickListener(new ClickEvent());
			btnSearch = (Button) this.findViewById(R.id.btnSearch);
			btnSearch.setOnClickListener(new ClickEvent());
			btnSend = (Button) this.findViewById(R.id.btnSend);
			btnSend.setOnClickListener(new ClickEvent());
            btn = (Button) this.findViewById(R.id.button);
            btn.setOnClickListener(new ClickEvent());
            back = (Button) this.findViewById(R.id.back);
            back.setOnClickListener(new ClickEvent());
            textv = (TextView) this.findViewById(R.id.editText);
			btnClose = (Button) this.findViewById(R.id.btnClose);
			btnClose.setOnClickListener(new ClickEvent());
			edtContext = (EditText) findViewById(R.id.txt_content);
			btnClose.setEnabled(false);
			btnSend.setEnabled(false);
			btnSendDraw.setEnabled(false);
		} catch (Exception ex) {
            Log.e("������Ϣ",ex.getMessage());
		}
		mConnPaireDev = new  ConnectPaireDev();
		mConnPaireDev.start();
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mService != null) 
			mService.stop();
		mService = null; 
	}
	
	class ClickEvent implements View.OnClickListener {
		public void onClick(View v) {
			if (v == btnSearch) {
                Intent serverIntent = new Intent(PrintDemo.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            } else if (v == btn) {
                /* open url */
                Button print_btn = (Button) findViewById(R.id.btnSend);
                Button search_btn = (Button) findViewById(R.id.btnSearch);
                ProgressBar loading = (ProgressBar) findViewById(R.id.progressBar);
                loading.setVisibility(View.VISIBLE);
                print_btn.setVisibility(View.INVISIBLE);
                search_btn.setVisibility(View.INVISIBLE);
                openUrl(textv.getText().toString());
            } else if (v == back){
                /* web view back */
                WebView mywebview = (WebView) findViewById(R.id.webView);
                mywebview.goBack();
			} else if (v == btnSend) {
                WebView mywebview = (WebView) findViewById(R.id.webView);
                Button print_btn = (Button) findViewById(R.id.btnSend);
                ProgressBar loading = (ProgressBar) findViewById(R.id.progressBar);
                loading.setVisibility(View.VISIBLE);
                loading.setProgress(10);
                //print_btn.setVisibility(View.INVISIBLE);
                Log.d("Print_clicked", "start downloading file -> " + mywebview.getUrl().toString());
                String msg = edtContext.getText().toString();
                if( msg.length() > 0 ){
                    mService.sendMessage(msg, "GBK");
                }
			} else if (v == btnClose) {
				mService.stop();
			} else if (v == btnSendDraw) {
                String msg = "";
                String lang = getString(R.string.strLang);
				printImage();
				
            	byte[] cmd = new byte[3];
        	    cmd[0] = 0x1b;
        	    cmd[1] = 0x21;
            	if((lang.compareTo("en")) == 0){	
            		cmd[2] |= 0x10;
            		mService.write(cmd);
            		mService.sendMessage("Congratulations!\n", "GBK"); 
            		cmd[2] &= 0xEF;
            		mService.write(cmd);
            		msg = "  You have sucessfully created communications between your device and our bluetooth printer.\n\n"
                          +"  Shenzhen Zijiang Electronics Co..Ltd is a high-tech enterprise which specializes" +
                          " in R&D,manufacturing,marketing of thermal printers and barcode scanners.\n\n"
                          +"  Please go to our website and see details about our company :\n" +"     http://www.zjiang.com\n\n";

            		mService.sendMessage(msg,"GBK");
            	}else if((lang.compareTo("ch")) == 0){
            		cmd[2] |= 0x10;
            		mService.write(cmd);
        		    mService.sendMessage("��ϲ��\n", "GBK"); 
            		cmd[2] &= 0xEF;
            		mService.write(cmd);
            		msg = "  ���Ѿ��ɹ��������������ǵ�������ӡ��\n\n"
            		+ "  �������ʽ��������޹�˾��һ��רҵ�����з��������������Ʊ�ݴ�ӡ�������ɨ���豸��һ��ĸ߿Ƽ���ҵ.\n\n"
            	    + "  ��ӭ���¼���ǵ���վ���鿴���ǹ�˾����ϸ��Ϣ:\n"+"     http://www.zjiang.com.\n\n";
            		mService.sendMessage(msg,"GBK");	
            	}
			}
		}
	}

    private final  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BluetoothService.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                	Toast.makeText(getApplicationContext(), "Connect successful",
                            Toast.LENGTH_SHORT).show();
        			btnClose.setEnabled(true);
        			btnSend.setEnabled(true);
        			btnSendDraw.setEnabled(true);
        			conn_flag = 1;
                    break;
                case BluetoothService.STATE_CONNECTING:
                	Log.d("��������","��������.....");
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                	Log.d("��������","�ȴ�����.....");
                    break;
                }
                break;
            case BluetoothService.MESSAGE_CONNECTION_LOST:
                Toast.makeText(getApplicationContext(), "Device connection was lost",
                               Toast.LENGTH_SHORT).show();
    			btnClose.setEnabled(false);
    			btnSend.setEnabled(false);
    			btnSendDraw.setEnabled(false);
                break;
            case BluetoothService.MESSAGE_UNABLE_CONNECT:
            	Toast.makeText(getApplicationContext(), "Unable to connect device",
                        Toast.LENGTH_SHORT).show();
            	conn_flag = -1;
            	break;
            }
        }
        
    };
        
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {    
        switch (requestCode) {
        case REQUEST_ENABLE_BT:
            if (resultCode == Activity.RESULT_OK) {
            	Toast.makeText(this, "Bluetooth open successful", Toast.LENGTH_LONG).show();
            } else {
            	finish();
            }
            break;
        case  REQUEST_CONNECT_DEVICE:
        	if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                con_dev = mService.getDevByMac(address);   
                
                mService.connect(con_dev);
            }
            break;
        }
    } 

    @SuppressLint("SdCardPath")
	private void printImage() {
    	byte[] sendData = null;
    	PrintPic pg = new PrintPic();
    	pg.initCanvas(576);     
    	pg.initPaint();
    	pg.drawImage(0, 0, "/mnt/sdcard/icon.jpg");
    	//
    	sendData = pg.printDraw();
    	mService.write(sendData);
    	Log.d("��������",""+sendData.length);
    }
    
    public class ConnectPaireDev extends Thread {
    	public void run(){
    		while(true)
    		    if( mService.isBTopen() == true)
    			    break;

    		Set<BluetoothDevice> pairedDevices = mService.getPairedDev();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                	if(conn_flag == 1){
                		conn_flag = 0;
                		break;
                	}
                	while(true)
                		if(conn_flag==-1 || conn_flag==0)
                			break;
                	mService.connect(device);
                	conn_flag = 2;
                }
            } 
    	}    	
    }
}
