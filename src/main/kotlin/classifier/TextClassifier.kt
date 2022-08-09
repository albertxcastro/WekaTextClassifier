package classifier

import extension.loadArff
import extension.loadDataset
import extension.saveArff
import weka.classifiers.Evaluation
import weka.classifiers.bayes.NaiveBayesMultinomial
import weka.classifiers.meta.FilteredClassifier
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances
import weka.core.tokenizers.NGramTokenizer
import weka.filters.unsupervised.attribute.StringToWordVector
import java.io.File


class TextClassifier(
    var classifier: FilteredClassifier? = null,
    private var trainingData: Instances? = null,
    private var wekaAttributes: ArrayList<Attribute>? = null
) {
    private val SPOFITY_TRAINING_DATA = "/dataset/spotify_reviews_training_dataset.txt"
    private val SPOFITY_TRAINING_ARFF_DATA = "/dataset/spotify_reviews_training_dataset.arff"
    private val TESTING_DATASET = "/dataset/spotify_reviews_test_dataset.txt"
    private val TESTING_DATASET_ARFF = "/dataset/spotify_reviews_training_dataset.arff"

    init {
        classifier = FilteredClassifier().apply {
            classifier = NaiveBayesMultinomial()
        }

        val attributeText = Attribute("text", null as List<String?>?)
        val classAttributeValues = arrayListOf("1", "2", "3", "4", "5")
        val classAttribute = Attribute("label", classAttributeValues)
        wekaAttributes = arrayListOf(classAttribute, attributeText)
    }

    fun transform() {
        try {
            trainingData = SPOFITY_TRAINING_DATA.loadDataset(wekaAttributes)
            SPOFITY_TRAINING_ARFF_DATA.saveArff(trainingData)

            val tokenizer = NGramTokenizer().apply {
                nGramMinSize = 1
                nGramMaxSize = 1
                delimiters = "\\W"
            }

            val filter = StringToWordVector().apply {
                attributeIndices = "last"
                setTokenizer(tokenizer)
                lowerCaseTokens = true
            }

            classifier?.filter = filter
        } catch (err: Exception) {
            println(err.message)
        }
    }

    fun fit() {
        try {
            classifier?.buildClassifier(trainingData)
        } catch (err: Exception) {
            println(err.message)
        }
    }

    fun predict(text: String?): String? {
        return try {
            val newDataset = Instances("predictiondata",wekaAttributes,1).apply {
                setClassIndex(0)
            }

            val newInstance = DenseInstance(2).apply {
                setDataset(newDataset)
            }

            newInstance.setValue(wekaAttributes!![1], text)
            val prediction = classifier!!.classifyInstance(newInstance)
            newDataset.classAttribute().value(prediction.toInt())
        } catch (err: Exception) {
            println(err.message)
            null
        }
    }

    fun evaluate(): String? {
        return try {
            val testingData = if (File(TESTING_DATASET_ARFF).exists()) {
                TESTING_DATASET_ARFF.loadArff()?.apply {
                    setClassIndex(0)
                }
            } else {
                TESTING_DATASET.loadDataset(wekaAttributes).apply {
                    TESTING_DATASET_ARFF.saveArff(this)
                }
            }
            val evaluation = Evaluation(testingData)
            evaluation.evaluateModel(classifier, testingData)
            evaluation.toSummaryString()
        } catch (err: Exception) {
            println(err.message)
            null
        }
    }
}