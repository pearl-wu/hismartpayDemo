package com.bais.hismart;

import java.security.PublicKey;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import org.apache.cordova.*;
import org.json.JSONException;

public class MainActivity extends CordovaPlugin {
    	
    	  protected static final String LOG_TAG = "hisenseOSOrder__pay";
		  private static final int RESULT_OK = 0;
    	  private String payResult,trade_no,packageName,platformId,paymentMD5;
    	  private ContentResolver mContentResolver = null;
    	  private String MD5Key="";//由海信分配每个应用的key不同
	
    	  public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException{
    		      			
    		if(action.equals("Pay")){			

    			if(checkPackageInfo(cordova.getActivity(), "com.hisense.hitv.payment")){
    				// final JSONObject options = args.getJSONObject(0);
    		        mContentResolver = cordova.getActivity().getContentResolver();
    		        packageName = cordova.getActivity().getPackageName();
    		        packageName = "";
    				
    				
                    Intent intent = new Intent();
                    //intent.setAction("com.hisense.hitv.payment.MAIN");//应用启动的action
                    intent.setAction("com.hisense.hitv.payment.QC");//应用启动的action
                    intent.putExtra("platformId", "");//支付平台1-支付宝2-微信（可空）
                    intent.putExtra("appName", ""); //应用名称
                    intent.putExtra("packageName", packageName);//包名               
                    intent.putExtra("paymentMD5Key", paymentMD5);//包名md5签名
                    intent.putExtra("tradeNum", "");//商品流水号，第三方商品唯一编号
                    intent.putExtra("goodsPrice", "0.01");//商品价格单位元，注意请转化成字符串
                    intent.putExtra("goodsName", "测试PA1");//商品名称
                    intent.putExtra("alipayUserAmount", "hsyzf@hisense.com");//收款账户
                    intent.putExtra("notifyUrl", "http://10.0.64.107:9080/notify");//第三方后台回调地址              
                    try {
                        cordova.getActivity().startActivityForResult(intent, 1);
                        } catch (ActivityNotFoundException e) {
                                Log.d("TAG", "出现异常版本过低，进入市场升级");
                                e.printStackTrace();
                                Intent it = new Intent(Intent.ACTION_VIEW);
                                it.setData(Uri.parse("himarket://details?id="+"com.hisense.hitv.payment"+"&isAutoDownload="+ 1));
                                cordova.getActivity().startActivity(it);
                    }

                }else{//如果没有预装海信支付跳转至海信聚好用下载
                    Log.d("TAG","未找到支付程序");
                    Intent it = new Intent(Intent.ACTION_VIEW);
                    it.setData(Uri.parse("himarket://details?id="+ "com.hisense.hitv.payment"));                            
                    cordova.getActivity().startActivity(it);
                }   			

    	       return true;
    		}else if(action.equals("Iandroid")){
    			boolean tr = args.getBoolean(0);
    			if(tr){
    			  String androidId = Secure.getString(cordova.getActivity().getContentResolver(), Secure.ANDROID_ID);
    			  Resultecho(true, androidId, callbackContext);
    			}
    		  return true;
    		}else if(action.equals("Packageinfo")){
    			int no = args.getInt(0);
    			String packageinfo="";
    			if(no==1){
    				//packageName
    				packageinfo = cordova.getActivity().getPackageName();				    				
    			}else if(no==2){
    				//strVersionCode
    		        try {
    		            PackageInfo packageInfo = cordova.getActivity().getPackageManager().getPackageInfo(cordova.getActivity().getPackageName(),0);
    		            packageinfo = String.valueOf(packageInfo.versionCode);
    		        } catch (NameNotFoundException e) {
    		            // TODO Auto-generated catch block
    		            e.printStackTrace();
    		        }  				
    			}else if(no==3){
    		        try {
    		        	PackageInfo packageInfo = cordova.getActivity().getPackageManager().getPackageInfo(cordova.getActivity().getPackageName(),0);
    		        	packageinfo = packageInfo.versionName;
    		        } catch (NameNotFoundException e) {
    		            // TODO Auto-generated catch block
    		            e.printStackTrace();
    		            packageinfo = "Cannot load Version!";
    		        }  	  				
    			}	
    			Resultecho(true, packageinfo, callbackContext);
    			return true;
    		}else if(action.equals("Echo")){
    			String context = args.getString(0);
    			int duration = args.getInt(1);
    			
    			Toast.makeText(cordova.getActivity(), context, duration).show(); 
    		
    			return true;
    		}else if(action.equals("Sign")){   	
    			    			
    			String mag = RSASign.sign(args.getString(0), Config.getPrikey(), "utf-8" );
    			Resultecho(true, mag, callbackContext);

    			return true;
    		}    		
    		
    	     return false;
    	  
    	  }    	  
			private ContextWrapper getResources() {
				// TODO Auto-generated method stub
				return null;
			}


			public void Resultecho(Boolean boo, String meg, CallbackContext callbackContext){
    		   if(boo){
    	           callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, meg));
    		   } else {
    		       callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, meg));
    		   }			
    		}
			
			
			public void onActivityResult(int requestCode, int resultCode, Intent data) {
		        super.onActivityResult(requestCode, resultCode, data);

		        if(resultCode==RESULT_OK){          
		            payResult = data.getStringExtra("payResult");//支付结果
		            Log.d("test", "payResult is "+payResult);
		            platformId =data.getStringExtra("platformId");//还需返回支付平台，
		            Log.d("test", "platformId is "+platformId);
		            trade_no =data.getStringExtra("trade_no");//支付流水号
		            Log.d("test", "trade_no is "+trade_no);
		        }
		    }
			
			
			/**
		     * 本地查找apk 是否存在
		     * 
		     * @param packageName 包名
		     * */
		    public static boolean checkPackageInfo(Context mContext,String packageName) {
		        if (packageName == null || "".equals(packageName))
		            return false;
		        try {
		            ApplicationInfo app = mContext.getPackageManager()
		                    .getApplicationInfo(packageName,
		                            PackageManager.GET_UNINSTALLED_PACKAGES);
		            if (app != null) {
		                Log.d("TAG","App info: " + app.flags);
		                Log.d("TAG","System App: "
		                        + ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 || (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0));
		                return true;
		            } else {
		                return false;
		            }
		        } catch (NameNotFoundException e) {
		            return false;
		        }
		    }
		    
		    /**
		     * 判断是否有网络连接
		     * 
		     * @return
		     */
		    public boolean isNetworkAvailable() {
		        ConnectivityManager cm = (ConnectivityManager) cordova.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		        NetworkInfo info = cm == null ? null : cm.getActiveNetworkInfo();
		        return (info != null && info.isConnected());
		    }
		    /**
		     * 弹出框，网络不通
		     * 
		     * @param Activity mActivity
		     * */
		    public static void ToastUI(Context mContext, Activity mActivity,String text) {
		        Toast mtoast = new Toast(mContext);
		        Toast.makeText(mContext, text, 5);
		        mtoast.show();
		    }
    }
