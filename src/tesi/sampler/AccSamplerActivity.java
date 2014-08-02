package tesi.sampler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

public class AccSamplerActivity extends Activity {
    private String currentLabel="staticoSulTavolo";
    private long countDown=5000;
	private long cont=10000;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        EditText text= (EditText)findViewById(R.id.durata);
        text.setWidth(100);
        text= (EditText)findViewById(R.id.ritardo);
        text.setWidth(100);
        RadioGroup group=(RadioGroup)findViewById(R.id.radioGroup);
        RadioGroup.OnCheckedChangeListener listener=new RadioGroup.OnCheckedChangeListener() {
			
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==R.id.stradio)
					currentLabel="staticoSulTavolo";
				if(checkedId==R.id.spradio)
					currentLabel="staticoInTasca";
				else if(checkedId==R.id.wradio)
					currentLabel="camminando";
				else if(checkedId==R.id.rradio)
					currentLabel="correndo";
			}
		};
		group.setOnCheckedChangeListener(listener);
    }

	
	
	public void start (View v)
	{
		countDown=5000;
		cont=10000;
		if(!((EditText)findViewById(R.id.ritardo)).getText().toString().equals(""))
		  countDown= Long.parseLong(((EditText)findViewById(R.id.ritardo)).getText().toString())*1000;
		if(!((EditText)findViewById(R.id.durata)).getText().toString().equals(""))
		  cont=Long.parseLong(((EditText)findViewById(R.id.durata)).getText().toString())*1000;
		Intent intent=new Intent();
		intent.setAction(AccSamplerService.ACCSAMPLERON_ACTION);
		Bundle bundle=new Bundle();
		bundle.putString("currentLabel", currentLabel);
		bundle.putLong("countDown",countDown);
		bundle.putLong("cont", cont);
		intent.putExtras(bundle);
		startService(intent);
	}
	
	public void stop(View v)
	{
		Intent intent = new Intent();
		intent.setAction(AccSamplerService.ACCSAMPLEROFF_ACTION);
		stopService(intent);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
}