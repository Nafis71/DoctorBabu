import pandas as pd
import joblib
from os.path import dirname, join
# importwarnings
import numpy as np

# warnings.filterwarnings("ignore")
from collections import Counter
from os.path import dirname, join


def main(givenDisease):
    diseases = np.array(givenDisease)
    return test_input(diseases)


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
    symptoms = ["itching", "skin_rash", "nodal_skin_eruptions", "dischromic_patches"]
    predicted = []
    global test_df, res
    predicted.clear()
#     symptoms.append("high_fever")
#     symptoms.append("chills")
#     symptoms.append("mild_fever")
    #     num_inputs = int(input("Enter the number of symptoms you have: "))
    #     for i in range(num_inputs):
    #         user_input = input("Enter Symptoms #{}: ".format(i + 1))
    #         symptoms.append(user_input)
    #     symptoms = np.loadtxt("testfile.csv",
    #                      delimiter=",", dtype=str)

    for column in test_col:
        test_data[column] = 1 if column in symptoms else 0
        test_df = pd.DataFrame(test_data, index=[0])
    tempModels = joblib.load(models)
    for j in range(4):
        model = tempModels[j]
        predict_disease = model.predict(test_df.values)
        predict_disease = le.inverse_transform(predict_disease)
        predicted.extend(predict_disease)
    disease_counts = Counter(predicted)
    percentage_per_disease = {disease: (count / 4) * 100 for disease, count in disease_counts.items()}
    diseaseKey = list(percentage_per_disease.keys())
    result = ""
    for i in range(len(diseaseKey)):
        if percentage_per_disease[diseaseKey[i]] > 50:
            result = diseaseKey[i]
            break

    return result


def predictDisease(model, le):
    predict_disease = model.predict(test_df.values)
    return le.inverse_transform(predict_disease)
    #
    # # result_df = pd.DataFrame({"Disease": list(percentage_per_disease.keys()),
    # #                           "Chances": list(percentage_per_disease.values())})
    # # result_df = pd.DataFrame({"Disease": list(percentage_per_disease.keys())})
    # result = result_df.to_numpy()
    # # print(result)
    # # result = result.tolist()
    # # result = result[0]
    # # txt = ""
    # # for x in result:
    # #     txt += x
    # # return txt
