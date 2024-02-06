import pandas as pd
import joblib
from os.path import dirname, join
import numpy as np

from collections import Counter
from os.path import dirname, join


def main(givenDisease):
    return test_input(givenDisease)


def test_input(givenDisease):
    diseaseSymptomsData = join(dirname(__file__), "diseaseSymptomsData.pkl")
    diseaseEncoder = join(dirname(__file__), "diseaseEncoder.pkl")
    models = join(dirname(__file__), "models.pkl")
    # loadingSymptoms data
    dis_sym_data_v1 = joblib.load(diseaseSymptomsData)

    # EncodingDisease

    var_mod = ['Disease']
    le = joblib.load(diseaseEncoder)

    test_col = []
    for col in dis_sym_data_v1.columns:
        if col != 'Disease':
            test_col.append(col)

    test_data = {}
    predicted = []
    global test_df, res
    predicted.clear()
    for column in test_col:
        test_data[column] = 1 if column in givenDisease else 0
        test_df = pd.DataFrame(test_data, index=[0])
    tempModels = joblib.load(models)
    for j in range(5):
        model = tempModels[j]
        predict_disease = model.predict(test_df.values)
        predict_disease = le.inverse_transform(predict_disease)
        predicted.extend(predict_disease)
    disease_counts = Counter(predicted)
    percentage_per_disease = {disease: (count / 5) * 100 for disease, count in disease_counts.items()}
    diseaseKey = list(percentage_per_disease.keys())
    result = ""
    for i in range(len(diseaseKey)):
        if percentage_per_disease[diseaseKey[i]] > 50:
            result = diseaseKey[i]
            break

    return percentage_per_disease
