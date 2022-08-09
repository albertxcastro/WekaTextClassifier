package extension

import weka.classifiers.meta.FilteredClassifier
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances
import weka.core.converters.ArffLoader
import weka.core.converters.ArffSaver
import java.io.*

fun String?.loadDataset(wekaAttributes: ArrayList<Attribute>?): Instances {
    val dataset = Instances("1 2 3 4 5", wekaAttributes, 8).apply {
        setClassIndex(0)
    }

    try {
        val text = this?.javaClass?.getResource(this)?.readText()
        val lines = text?.split("\n")
        lines?.forEach { line ->
            val parts = line.split(",", limit = 2).toTypedArray()
            if (parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
                val row = DenseInstance(2)
                row.setValue(wekaAttributes!![0], parts[0])
                row.setValue(wekaAttributes[1], parts[1])
                dataset.add(row)
            }
        }
    } catch (e: IOException) {
        println(e.message)
    } catch (e: ArrayIndexOutOfBoundsException) {
        println("Bad row detected!. ${e.message}")
    }
    return dataset
}

fun String?.loadArff(): Instances? {
    return try {
        val reader = BufferedReader(FileReader(this))
        val arff = ArffLoader.ArffReader(reader)
        val dataset = arff.data
        reader.close()
        dataset
    } catch (e: IOException) {
        println(e.message)
        null
    }
}

fun String.loadModel(): FilteredClassifier? {
    return try {
        val stream = ObjectInputStream(FileInputStream(this))
        val classifier = stream.readObject() as FilteredClassifier
        stream.close()
        println("Model successfully loaded: $this")
        classifier
    } catch (e: FileNotFoundException) {
        println(e.message)
        null
    } catch (e: IOException) {
        println(e.message)
        null
    } catch (e: ClassNotFoundException) {
        println(e.message)
        null
    }
}

fun String.saveModel(classifier: FilteredClassifier) {
    try {
        val out = ObjectOutputStream(FileOutputStream(this))
        out.writeObject(classifier)
        out.close()
        print("Saved model: $this")
    } catch (e: IOException) {
        print(e.message)
    }
}

fun String?.saveArff(dataset: Instances?) {
    try {
        val arffSaverInstance = ArffSaver()
        arffSaverInstance.instances = dataset
        arffSaverInstance.setFile(File(this))
        arffSaverInstance.writeBatch()
    } catch (e: IOException) {
        println(e.message)
    }
}