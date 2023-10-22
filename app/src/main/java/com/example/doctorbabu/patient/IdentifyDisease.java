package com.example.doctorbabu.patient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityIdentifyDiseaseBinding;
import com.google.android.material.chip.Chip;
import com.thekhaeng.pushdownanim.PushDownAnim;

import org.aviran.cookiebar2.CookieBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdentifyDisease extends AppCompatActivity {
    Animation leftAnimation, fadein;
    ActivityIdentifyDiseaseBinding binding;
    ArrayList<String> symptomsList = new ArrayList<>();
    ArrayList<String> transformedSymptomsList = new ArrayList<>();
    ExecutorService backgroundExecutor, scriptExecutor;
    String predictedResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIdentifyDiseaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.analyze.setVisibility(View.INVISIBLE);
        binding.infoCard.setVisibility(View.GONE);
        leftAnimation = AnimationUtils.loadAnimation(this, R.anim.left_animation);
        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        backgroundExecutor = Executors.newSingleThreadExecutor();
        scriptExecutor = Executors.newSingleThreadExecutor();
        PushDownAnim.setPushDownAnimTo(binding.analyze).setScale(PushDownAnim.MODE_SCALE, 0.95f);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setSymptomsAdapter();
        analyzeButtonListener();
    }

    public void setSymptomsAdapter() {
        binding.symptoms.setThreshold(3);
        String dataset = "cold_hands_and_feets\tirritability\trestlessness\tloss_of_balance\tloss_of_appetite\tcontinuous_feel_of_urine\textra_marital_contacts\thip_joint_pain\tbreathlessness\tpain_during_bowel_movements\tnodal_skin_eruptions\tulcers_on_tongue\tphlegm\tspinning_movements\tabdominal_pain\thigh_fever\tred_sore_around_nose\tacidity\tneck_pain\tbladder_discomfort\tswollen_blood_vessels\tscurring\tyellow_crust_ooze\tbruising\tweakness_of_one_body_side\tdehydration\tjoint_pain\tredness_of_eyes\tindigestion\tsmall_dents_in_nails\tobesity\tknee_pain\tfast_heart_rate\tbrittle_nails\tswollen_extremeties\tmuscle_pain\ttoxic_look_(typhos)\tchest_pain\tstomach_bleeding\tstiff_neck\tsinus_pressure\tirritation_in_anus\tblackheads\tblister\tirregular_sugar_level\tlack_of_concentration\tyellow_urine\tbelly_pain\tNaN\tdischromic _patches\tacute_liver_failure\tstomach_pain\tabnormal_menstruation\tcontinuous_sneezing\tbloody_stool\tvomiting\tconstipation\tpalpitations\tyellowish_skin\tred_spots_over_body\tburning_micturition\tcough\tnausea\tspotting_ urination\tdepression\tskin_peeling\tmucoid_sputum\tfamily_history\tinternal_itching\tpatches_in_throat\tblood_in_sputum\tshivering\tprominent_veins_on_calf\tincreased_appetite\tanxiety\theadache\tsunken_eyes\tyellowing_of_eyes\trusty_sputum\tpolyuria\tswelling_of_stomach\tdrying_and_tingling_lips\tfoul_smell_of urine\tweakness_in_limbs\tchills\tmalaise\tpain_in_anal_region\tpassage_of_gases\tsilver_like_dusting\treceiving_blood_transfusion\tweight_gain\trunny_nose\tloss_of_smell\tlethargy\tswelling_joints\tpuffy_face_and_eyes\tskin_rash\tdizziness\tfatigue\tdark_urine\tthroat_irritation\tswelled_lymph_nodes\treceiving_unsterile_injections\tmuscle_weakness\tswollen_legs\tcramps\titching\tmood_swings\tslurred_speech\tback_pain\thistory_of_alcohol_consumption\tdiarrhoea\taltered_sensorium\tinflammatory_nails\tmild_fever\twatering_from_eyes\tblurred_and_distorted_vision\tdistention_of_abdomen\tvisual_disturbances\tpain_behind_the_eyes\tfluid_overload\tweight_loss\tunsteadiness\tpus_filled_pimples\tenlarged_thyroid\tcongestion\tcoma\tmuscle_wasting\texcessive_hunger\tpainful_walking\tmovement_stiffness\tsweating";
        dataset = dataset.replaceAll("_", " ").toUpperCase(Locale.ROOT);
        String[] namesOfSymptoms = dataset.split("\t");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(IdentifyDisease.this, R.layout.drop_menu, namesOfSymptoms);
        binding.symptoms.setAdapter(adapter);
        binding.symptoms.setOnClickListener(view -> binding.symptoms.dismissDropDown());
        setSymptomsTextWatcher();
    }

    public void setSymptomsTextWatcher() {
        binding.symptoms.setOnItemClickListener((adapterView, view, i, l) -> {
            String chipName = binding.symptoms.getText().toString().trim();
            if (!symptomsList.contains(chipName)) {
                symptomsList.add(chipName);
                Log.w("SymptomsList:", symptomsList.toString());
                binding.symptoms.setText(null);
                symptomsChipMaker(chipName);
            } else {
                CookieBar.build(IdentifyDisease.this).setTitle("Duplicate Input").setMessage("Given symptom is already added!").setSwipeToDismiss(true).setDuration(3000).setTitleColor(R.color.white).setBackgroundColor(R.color.blue).setCookiePosition(CookieBar.TOP)  // Cookie will be displayed at the Top
                        .show();
            }
        });
    }

    public void symptomsChipMaker(String chipName) {
        Random random = new Random();
        @SuppressLint("InflateParams")
        Chip chip = (Chip) LayoutInflater.from(IdentifyDisease.this).inflate(R.layout.chip_layout, null);
        chip.setText(chipName);
        chip.setId(random.nextInt());
        chip.setHeight(80);
        chip.setClickable(false);
        binding.chipGroup.addView(chip);
        chip.setOnCloseIconClickListener(view -> {
            binding.chipGroup.removeView(chip);
            symptomsList.remove(chip.getText().toString());
            hideAndShowAnalyzeButton();
        });
        hideAndShowAnalyzeButton();
    }

    public void hideAndShowAnalyzeButton() {
        if (symptomsList.size() >= 4) {
            binding.analyze.setVisibility(View.VISIBLE);
            binding.infoCard.setVisibility(View.GONE);
        } else if (symptomsList.size() == 0) {
            binding.analyze.setVisibility(View.INVISIBLE);
            binding.infoCard.setVisibility(View.GONE);
        } else {
            binding.analyze.setVisibility(View.INVISIBLE);
            binding.infoCard.setVisibility(View.VISIBLE);
            binding.infoCard.setAnimation(leftAnimation);
        }
    }

    public void analyzeButtonListener() {
        binding.analyze.setOnClickListener(view -> {
            removeViews();
            backgroundExecutor.execute(this::transformSymptomList);

        });
    }

    public void removeViews() {
        binding.symptomsImage.setVisibility(View.GONE);
        binding.symptomsLayout.setVisibility(View.GONE);
        binding.chipGroup.setVisibility(View.GONE);
        binding.headerText3.setVisibility(View.GONE);
        binding.headerText.setVisibility(View.GONE);
        binding.headerText2.setVisibility(View.GONE);
        binding.analyze.setVisibility(View.GONE);
        showViews();
    }

    public void showViews() {
        binding.loadingAnimation.setVisibility(View.VISIBLE);
        binding.loadingTextCard.setVisibility(View.VISIBLE);
        binding.loadingAnimation.setAnimation(fadein);
        binding.loadingTextCard.setAnimation(leftAnimation);
    }

    public void generateResult() {
        backgroundExecutor.shutdown();
        scriptExecutor.execute(() -> {
            String[] symptomsArrayList = transformedSymptomsList.toArray(new String[0]);
            Log.w("ArrayList:", Arrays.toString(symptomsArrayList));
            startPython();
            Python python = Python.getInstance();
            PyObject module = python.getModule("predictor");
            PyObject prediction = module.callAttr("main", (Object) symptomsArrayList);
            if (prediction.toString().equals("")) {
                predictedResult = "No Result";
            } else {
                predictedResult = prediction.toString();
            }
            runOnUiThread(() -> {
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    Intent intent = new Intent(IdentifyDisease.this, predictedDisease.class);
                    intent.putExtra("predictedDisease", predictedResult);
                    startActivity(intent);
                    finish();
                }, 2000);
            });

        });

    }

    public void startPython() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(IdentifyDisease.this));
        }
    }

    public void transformSymptomList() {
        for (String symptom : symptomsList) {
            transformedSymptomsList.add(symptom.replaceAll(" ", "_").toLowerCase());
        }

        generateResult();

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scriptExecutor.shutdown();
        binding = null;
    }
}