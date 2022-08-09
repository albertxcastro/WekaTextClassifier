
import classifier.TextClassifier
import extension.loadModel
import extension.saveModel
import java.io.File

fun main(args: Array<String>) {
        val textClassifier = TextClassifier()
    val model = "data_model.dat"

    if (File(model).exists()) {
        textClassifier.classifier = model.loadModel()
    } else {
        textClassifier.transform()
        textClassifier.fit()
        textClassifier.classifier?.let { model.saveModel(it) }
    }

    println("Evaluation Result: ${textClassifier.evaluate()}".trimIndent())

    var input = ""
    while (input != "exit") {
        println("Enter your custom review: ")
        input = readln()
        val predictedClass = textClassifier.predict(input)
        print("predicted class: $predictedClass stars\n")
    }
}