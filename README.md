A part of a larger team project in Java. This section is an attempt to classify characters from the Super Mario game series. Six different classes are detected: Mario, Luigi, Toad, Bowser, Goomba, Wario. 

First, a Java class ImageFeatures (src/classifiers/ImageFeatures.java)
extracts approximated pixel color information from a learning image set under ./dataset (images not included due to copyright reasons). Note: this method is only applicable to images with white background. The extracted information is then written to dataset.csv.

Then, an Octave script learning.ex is used to learn the paramemeters of the model using the feature data in dataset.csv (pre-learned parameters included in mean, std and theta*.csv). The Octave script is only run if  one of the parameter CSVs is missing or defective.

Finally a Java class BasicClassifier (src/classifiers/BasicClassifier.java) uses the learned parameters in predicting the characters presented in images under ./testset.
