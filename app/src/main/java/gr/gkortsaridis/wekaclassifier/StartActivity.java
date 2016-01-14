package gr.gkortsaridis.wekaclassifier;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends Activity {

    private Spinner classifier, data, folds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        classifier = (Spinner) findViewById(R.id.classifier_spinner);
        data = (Spinner) findViewById(R.id.dataset_spinner);
        folds = (Spinner) findViewById(R.id.folds_spinner);

        List<String> list = new ArrayList<String>();
        list.add("Iris");
        list.add("Breast Cancer");
        list.add("Dermatology");
        list.add("Ecoli");
        list.add("Hepatitis");
        list.add("Labor");
        list.add("Liver Disorders");
        list.add("Lung Cancer");
        list.add("Lymph");
        list.add("Primary Tumor");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        data.setAdapter(dataAdapter);

        List<String> list1 = new ArrayList<String>();
        list1.add("IB1");
        list1.add("IB2");
        list1.add("IB5");
        list1.add("IB10");
        list1.add("J48");
        list1.add("PART");
        list1.add("DecisionTable");
        list1.add("DecisionStump");
        list1.add("KStar");
        list1.add("NaiveBayesUpdateable");
        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list1);
        dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classifier.setAdapter(dataAdapter1);

        List<String> list2 = new ArrayList<String>();
        list2.add("10");
        list2.add("9");
        list2.add("8");
        list2.add("7");
        list2.add("6");
        list2.add("5");
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list2);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        folds.setAdapter(dataAdapter2);
    }


    public void about(View v){
        startActivity(new Intent(StartActivity.this, AboutActivity.class));
    }

    public void classify(View v){
        Intent intent = new Intent(StartActivity.this , MainActivity.class);
        intent.putExtra("data",data.getSelectedItemPosition());
        intent.putExtra("classifier",classifier.getSelectedItemPosition());
        intent.putExtra("folds",folds.getSelectedItemPosition());
        startActivity(intent);
    }
}
