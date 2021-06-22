package com.ascs.mydoctor;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.CustomVisionPredictionClient;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.CustomVisionPredictionManager;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.ImagePrediction;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.Prediction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ImageClassifier extends AsyncTask<String, Void, String> {
    private static final String API_KEY = "API_key here";
    private final String PredictionKey = "prediction key here";
    private final String endpoint = "https://cogentiveservicetest.cognitiveservices.azure.com/";
    ByteArrayInputStream stream;
    TextView pridiction, recommendation, confidence;
    ProgressDialog dialog;
    Context context;


    private CustomVisionPredictionClient predictor = CustomVisionPredictionManager
            .authenticate("https://cogentiveservicetest.cognitiveservices.azure.com/customvision/v3.0/Prediction/", PredictionKey)
            .withEndpoint(endpoint);


    public ImageClassifier(Context context, ByteArrayInputStream inputStream1, TextView prediction, TextView confidence, TextView recommendation) {
        this.context = context;
        this.stream = inputStream1;
        this.pridiction = prediction;
        this.confidence = confidence;
        this.recommendation = recommendation;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setMessage("Image Analyzing...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

    }

    @Override
    protected String doInBackground(String... strings) {

        try {
            String iteration = "interaction name here";
            String projectId = "project Id here";
//            testProject(predictor, projectId, context, iteration,stream);

            byte[] image = this.read(stream);

            ImagePrediction results = predictor.predictions()
                    .classifyImage()
                    .withProjectId(UUID.fromString(projectId))
                    .withPublishedName(iteration)
                    .withImageData(image)
                    .execute();

            String result = "";
//
            for (Prediction prediction :
                    results.predictions()) {
                result += prediction.tagName() + "=";
                result += prediction.probability() + "\n";

            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        new ProgressDialog(context).dismiss();


        String[] data = s.split("\n");
        String[] subdata = data[0].split("=");
        String disease = subdata[0];
        String confidenceV = subdata[1];


        pridiction.setText(disease);
        Float conf = Float.parseFloat(confidenceV);
        conf = conf * 100;
        DecimalFormat df = new DecimalFormat("#.00");
        conf = Float.valueOf(df.format(conf));
        confidence.setText((conf) + " %");


        recommedationFetch(context, recommendation, disease);

    }

    private void recommedationFetch(Context context, final TextView recomTV, String searchKey) {

        dialog.setMessage("Recommendation...");

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "azure search url here" + searchKey;
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dialog.dismiss();
                        // response
                        try {
                            JSONObject object = new JSONObject(response);
                            recomTV.setText(object.getString("value"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();

                        // TODO Auto-generated method stub
                        Log.d("ERROR", "error => " + error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("api-key", API_KEY);
                params.put("content-type", "application/json");

                return params;
            }
        };
        queue.add(getRequest);


    }


    private byte[] read(ByteArrayInputStream bais) throws IOException {
        byte[] array = new byte[bais.available()];
        bais.read(array);

        return array;
    }
}

