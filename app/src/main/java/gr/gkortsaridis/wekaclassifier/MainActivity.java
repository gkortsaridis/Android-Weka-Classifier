package gr.gkortsaridis.wekaclassifier;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.core.FastVector;
import weka.core.Instances;

public class MainActivity extends Activity {

    TextView correctly,correctly_perc,incorrectly,incorrectly_perc,kappa,mae,rmse,rae,rrse,number,accuracy,detailed_accuracy,confusion_matrix,info;
    String result;

    Classifier[] models = {
            new IBk(),
            new IBk(2),
            new IBk(5),
            new IBk(10),
            new J48(), // a decision tree
            new PART(),
            new DecisionTable(),//decision table majority classifier
            new DecisionStump(), //one-level decision tree
            new KStar(),
            new NaiveBayesUpdateable()
    };

    int datas[] = {
            R.raw.iris,
            R.raw.breast_cancer,
            R.raw.dermatology,
            R.raw.ecoli,
            R.raw.hepatitis,
            R.raw.labor,
            R.raw.liver_disorders,
            R.raw.lung_cancer,
            R.raw.lymph,
            R.raw.primary_tumor
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        correctly = (TextView) findViewById(R.id.correctly_classified);
        correctly_perc = (TextView) findViewById(R.id.correctly_classified_perc);
        incorrectly = (TextView) findViewById(R.id.incorrectly_classified);
        incorrectly_perc = (TextView) findViewById(R.id.incorreclty_classified_perc);
        kappa = (TextView) findViewById(R.id.kappa);
        mae = (TextView) findViewById(R.id.mae);
        rmse = (TextView) findViewById(R.id.rmse);
        rae = (TextView) findViewById(R.id.rae);
        rrse = (TextView) findViewById(R.id.rrse);
        number = (TextView) findViewById(R.id.number_instances);
        accuracy = (TextView) findViewById(R.id.accuracy);
        detailed_accuracy = (TextView) findViewById(R.id.detailed_accuracy);
        confusion_matrix = (TextView) findViewById(R.id.confusion_matrix);
        info = (TextView) findViewById(R.id.info);

        int classifier = getIntent().getExtras().getInt("classifier");
        int folds = 10 - getIntent().getExtras().getInt("folds");
        int data  = getIntent().getExtras().getInt("data");

        try {
            result = Classify(classifier, datas[data] , folds);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String Classify(int which_model , int which_file , int folds) throws Exception {

        String output = "";

        InputStream is = getBaseContext().getResources().openRawResource(which_file);
        BufferedReader datafile = new BufferedReader(new InputStreamReader(is, "UTF8"));

        Instances data = new Instances(datafile);
        data.setClassIndex(data.numAttributes() - 1);

        // Do 10-split cross validation
        Instances[][] split = crossValidationSplit(data, folds);

        //Separate split into training and testing arrays
        Instances[] trainingSplits = split[0];
        Instances[] testingSplits = split[1];

        // Collect every group of predictions for current model in a FastVector
        FastVector predictions = new FastVector();

        Evaluation validation = null;

        // For each training-testing split pair, train and test the classifier
        for (int i = 0; i < trainingSplits.length; i++) {
            validation = classify(models[which_model], trainingSplits[i], testingSplits[i]);
            predictions.appendElements(validation.predictions());

            Random rand = new Random(1);  // using seed = 1
            validation.crossValidateModel(models[which_model], data, folds, rand);
        }

        // Calculate overall accuracy of current classifier on all splits
        double accuracyD = calculateAccuracy(predictions);
        accuracy.setText("Classifier Accuracy : "+String.format("%.2f%%", accuracyD));

        //Printing results of the model
        /*output += models[which_model].toString();

        output +="\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n";
        output +=  "~~~~~~~~~~~SUMMARY~~~~~~~~~~~\n";
        output +="~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n";
        output += validation.toSummaryString();

        output +="\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n";
        output +=  "~~~~~~~~~~~Class Details~~~~~~~~~~\n";
        output +="~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n";
        output += validation.toClassDetailsString();

        output +="\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n";
        output +=  "~~~~~~~~Simulation Matrix~~~~~~~~~~\n";
        output +="~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n";
        output += validation.toMatrixString();

        accur.setText(result);*/

        //output +="~~~~~~~~~~~~~~~~~~";
        //output += result + "\n";
        //System.out.println(models[which_model].toString());
        //System.out.println(validation.toSummaryString());
        //System.out.println(validation.toClassDetailsString());
        //System.out.println(validation.toMatrixString());
        //System.out.println(result);

        correctly.setText((int)validation.correct()+"");
        correctly_perc.setText(String.format("%.2f", validation.pctCorrect()));
        incorrectly.setText((int)validation.incorrect()+"");
        incorrectly_perc.setText(String.format("%.2f", validation.pctIncorrect()));
        kappa.setText(String.format("%.2f", validation.kappa()));
        mae.setText(String.format("%.2f", validation.meanAbsoluteError()));
        rmse.setText(String.format("%.2f", validation.rootMeanSquaredError()));
        rae.setText(String.format("%.2f", validation.relativeAbsoluteError()));
        rrse.setText(String.format("%.2f", validation.rootRelativeSquaredError()));
        number.setText((int)validation.numInstances()+"");

        detailed_accuracy.setText(validation.toClassDetailsString());
        confusion_matrix.setText(validation.toMatrixString());
        info.setText(models[which_model].toString());

        return output;
    }

    public static Evaluation classify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception {
        Evaluation evaluation = new Evaluation(trainingSet);

        model.buildClassifier(trainingSet);
        evaluation.evaluateModel(model, testingSet);

        return evaluation;
    }

    public static double calculateAccuracy(FastVector predictions) {
        double correct = 0;

        for (int i = 0; i < predictions.size(); i++) {
            NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
            if (np.predicted() == np.actual()) {
                correct++;
            }
        }

        return 100 * correct / predictions.size();
    }

    public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) {
        Instances[][] split = new Instances[2][numberOfFolds];

        for (int i = 0; i < numberOfFolds; i++) {
            split[0][i] = data.trainCV(numberOfFolds, i);
            split[1][i] = data.testCV(numberOfFolds, i);
        }

        return split;
    }

}
