package tesi.sampler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
//import android.widget.Toast;

public class AccSamplerService extends Service implements SensorEventListener {
	public static final String ACCSAMPLERON_ACTION = "tesi.sampler.action.ON";
	public static final String ACCSAMPLEROFF_ACTION = "tesi.sampler.action.OFF";
	public static final int NOTIFICATION_ID=1;
	private PowerManager.WakeLock mWl;
    private Calendar cal;
	private BufferedWriter writer;
    private String currentLabel;
    private Long countDown;
    private Long cont;
    private File DatiTesi;
    private Notification notification;
    private PendingIntent pIntent;
    private SensorManager sm;
    private NotificationManager nM;
	public static final AtomicBoolean isServiceRunning = new AtomicBoolean(false);
	private CountDownTimer count1;
	private CountDownTimer count2;
	private int contEventi;
	private boolean eventToWrite=false;
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	
	public void onCreate() {

		try {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, AccSamplerService.class.getName());
			mWl.acquire();
			contEventi=0;
			nM=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			File sdcardDir=Environment.getExternalStorageDirectory();
			DatiTesi=new File(sdcardDir,"DatiTesi");
			DatiTesi.mkdirs();
			File staticoSulTavolo=new File(DatiTesi,"staticoSulTavolo");
			staticoSulTavolo.mkdir();
			File staticoInTasca=new File(DatiTesi,"staticoInTasca");
			staticoInTasca.mkdir();
			File camminando=new File(DatiTesi,"camminando");
			camminando.mkdir();
			File correndo=new File(DatiTesi,"correndo");
			correndo.mkdir();
			AccSamplerService.isServiceRunning.set(false);
		} catch (Exception e) {
			e.printStackTrace();
			stopSelf();
		}

	}
	
	public void notifica()
	{
		notification=new Notification(R.drawable.ic_launcher, "AccSampler", System.currentTimeMillis());
	    notification.flags |= Notification.FLAG_NO_CLEAR;
	    notification.defaults |= Notification.DEFAULT_LIGHTS;
	    Intent intent= new Intent(this, AccSamplerActivity.class);
	    intent.putExtra("notificationType", "Notification");
	    pIntent= PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	    notification.setLatestEventInfo(this, "AccSampler", "Conteggio in corso..", pIntent);
	    nM.notify(NOTIFICATION_ID,notification);
	}
	

	public void onSensorChanged(SensorEvent event) {
		try {
			writer.write(System.currentTimeMillis()+","+event.values[0]+","+event.values[1]+","+event.values[2]+"\n");
			this.eventToWrite=true;
			this.contEventi++;
			if(this.contEventi==100)
			{
				writer.flush();
				contEventi=0;
				this.eventToWrite=false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onDestroy() {
		mWl.release();
		if(AccSamplerService.isServiceRunning.get()==true)
		{
		   AccSamplerService.isServiceRunning.set(false);
		   if(count1!=null)
		      count1.cancel();
		   if(count2!=null)
		      count2.cancel();
		   smettiDiCampionare();
//		   Toast.makeText(this, "Campionamento terminato.", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent);
    }
	

	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent!=null)
		   handleCommand(intent);
		return START_STICKY;
	}
	
	private void handleCommand(Intent intent)
	{
		if(intent.getAction().equals(ACCSAMPLERON_ACTION))
		{  
		   notifica();
		   Bundle extras=intent.getExtras();
		   this.currentLabel=extras.getString("currentLabel");
		   this.cont=extras.getLong("cont");
		   this.countDown=extras.getLong("countDown");
		   if(AccSamplerService.isServiceRunning.get()==false)
		   {
			  startSampling();
		   }
		}
		if(intent.getAction().equals(ACCSAMPLEROFF_ACTION))
		{
			if(AccSamplerService.isServiceRunning.get()==true)
			{
				stopSelf();
			}
		}
	}
	
	private void iniziaACampionare()
	{
	    try {
	    	notification.setLatestEventInfo(this, "AccSampler", "Campionamento in corso...", pIntent);
	    	nM.notify(NOTIFICATION_ID,notification);
	      	cal=Calendar.getInstance();
	       	String dirPath=DatiTesi.getAbsolutePath()+"/"+currentLabel;
	       	String nomeFile= Integer.toString(cal.get(Calendar.MONTH) + 1) + "_"+ Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + "_" + Integer.toString(cal.get(Calendar.HOUR_OF_DAY)) + "_" + Integer.toString(cal.get(Calendar.MINUTE)) + "_" + Integer.toString(cal.get(Calendar.SECOND)) + ".txt";
	       	File f=new File(dirPath,nomeFile);
			FileWriter fw = new FileWriter(f, true);
	        writer = new BufferedWriter(fw);  
			sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) , SensorManager.SENSOR_DELAY_FASTEST);
	    }catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void smettiDiCampionare() 
	{
		sm.unregisterListener(this);
		if(eventToWrite)
		{
			this.eventToWrite=false;
			try {
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		AccSamplerService.isServiceRunning.set(false);
		nM.cancelAll();
		stopSelf();
		
	}
	
	private void startSampling()
	{
//		Toast.makeText(this, "Conteggio iniziato...", Toast.LENGTH_SHORT).show();
		AccSamplerService.isServiceRunning.set(true);
		count1=new CountDownTimer(countDown,1000) {			

	  		  @Override
	  		  public void onFinish() {
	  	         iniziaACampionare();
//	  	         Toast.makeText(getApplicationContext(), "Campionamento in corso...", Toast.LENGTH_SHORT).show();
	  		   }

			@Override
			public void onTick(long millisUntilFinished) {
				
			}
	   }.start();
	   count2=new CountDownTimer(cont+countDown,1000)
	   {
     		@Override
			public void onFinish() {
     			smettiDiCampionare(); 
			}
		    
     		@Override
			public void onTick(long millisUntilFinished) {
				
			}		
		}.start();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
