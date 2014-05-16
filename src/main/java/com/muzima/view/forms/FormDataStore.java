package com.muzima.view.forms;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.muzima.MuzimaApplication;
import com.muzima.api.model.FormData;
import com.muzima.api.model.Patient;
import com.muzima.controller.ConceptController;
import com.muzima.controller.FormController;
import com.muzima.controller.PatientController;
import com.muzima.service.FormParser;

import com.muzima.utils.Constants;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;

import static com.muzima.utils.Constants.FORM_DISCRIMINATOR_REGISTRATION;
import static com.muzima.utils.Constants.STATUS_COMPLETE;

public class FormDataStore {
    private static final String TAG = "FormDataStore";

    private FormWebViewActivity formWebViewActivity;
    private FormController formController;
    private FormData formData;
    private MuzimaApplication applicationContext;

    public FormDataStore(FormWebViewActivity formWebViewActivity, FormController formController, FormData formData) {
        this.formWebViewActivity = formWebViewActivity;
        this.formController = formController;
        this.formData = formData;
        this.applicationContext = (MuzimaApplication) formWebViewActivity.getApplicationContext();

    }

    @JavascriptInterface
    public void save(String jsonData, String xmlData, String status) {
        Patient newPatient = null;
        if (isRegistrationComplete(status)) {
            newPatient = formController.createNewPatient(jsonData);
            formData.setPatientUuid(newPatient.getUuid());
            formWebViewActivity.startPatientSummaryView(newPatient);
        }
        Log.d(TAG, "xml data is:" + xmlData);
        formData.setXmlPayload(xmlData);
        Log.d(TAG, "json data is:" + jsonData);
        formData.setJsonPayload(jsonData);
        formData.setStatus(status);
        try {
//            parseForm(xmlData, status);
            formController.saveFormData(formData);
            formWebViewActivity.setResult(FormsActivity.RESULT_OK);
            formWebViewActivity.finish();
        } catch (FormController.FormDataSaveException e) {
            Toast.makeText(formWebViewActivity, "An error occurred while saving the form", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Exception occurred while saving form data" + e);
//        } catch (ConceptController.ConceptSaveException e) {
//            Toast.makeText(formWebViewActivity, "An error occurred while saving observations in the form", Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "Exception occurred while saving a concept parsed from the form data" + e);
//        } catch (ParseException e) {
//            Toast.makeText(formWebViewActivity, "An error occurred while saving observations in the form", Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "Exception occurred while parsing the xml payload" + e);
//        } catch (XmlPullParserException e) {
//            Toast.makeText(formWebViewActivity, "An error occurred while saving observations in the form", Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "Exception occurred while exploring the xml payload" + e);
//        } catch (PatientController.PatientLoadException e) {
//            Toast.makeText(formWebViewActivity, "An error occurred while saving observations in the form", Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "Exception occurred while loading a patient parsed from the form data" + e);
//        } catch (ConceptController.ConceptFetchException e) {
//            Toast.makeText(formWebViewActivity, "An error occurred while saving observations in the form", Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "Exception occurred while fetching a concept parsed from the form data" + e);
//        } catch (IOException e) {
//            Toast.makeText(formWebViewActivity, "An error occurred while saving observations in the form", Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "IOException occurred while saving observations parsed from the form data" + e);
        }
    }

    private void parseForm(String xmlData, String status) throws ConceptController.ConceptSaveException, ParseException, XmlPullParserException, PatientController.PatientLoadException, ConceptController.ConceptFetchException, IOException {
        if (status.equals(Constants.STATUS_INCOMPLETE)){
            return;
        }
        FormParser formParser = getFormParser();
        formParser.parseAndSaveObservations(xmlData,formData.getUuid());
    }

    public FormParser getFormParser() {
        return new FormParser(applicationContext.getPatientController(), applicationContext.getConceptController(), applicationContext.getEncounterController(), applicationContext.getObservationController());
    }


    private boolean isRegistrationComplete(String status) {
        return isRegistrationForm() && status.equals(STATUS_COMPLETE);
    }

    @JavascriptInterface
    public String getFormPayload() {
        return formData.getJsonPayload();
    }

    @JavascriptInterface
    public String getFormStatus() {
        return formData.getStatus();
    }

    @JavascriptInterface
    public void showSaveProgressBar() {
        formWebViewActivity.showProgressBar("Saving...");
    }

    public boolean isRegistrationForm() {
        return (formData.getDiscriminator() == null) ? false :
                formData.getDiscriminator().equals(FORM_DISCRIMINATOR_REGISTRATION);
    }
}
