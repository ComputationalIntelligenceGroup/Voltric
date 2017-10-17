package voltric.learning.structure.constraintbased;

import voltric.data.DiscreteData;
import voltric.learning.LearningResult;
import voltric.learning.parameter.DiscreteParameterLearning;
import voltric.learning.structure.DiscreteStructureLearning;
import voltric.model.DiscreteBayesNet;

/**
 * The PC algorithm returns a PDAG class, so to bale to return a single DAG model, we create this hybrid version that
 * applies a Hill-climbing algorithm to the returned PDAG object with the objective of generating a DAG model.
 *
 * TODO: Recordar que hay otras posibilidades como la propuesta por Chickering en la pag 454 (ultimo parrafo) de su articulo http://www.jmlr.org/papers/volume2/chickering02a/chickering02a.pdf
 * el cual se aplciaria tras el segundo paso del PC una vez generado un PDAG.
 */
//TODO: Esta implementación se hace siguiendo con un test de independencia de normalized MI y chi-square
public class HybridPC implements DiscreteStructureLearning {

    double p_value;

    public HybridPC(double p_value){
        this.p_value = p_value;
    }

    // La idea del PC, como de todos los constraint-based es de empezar creando un grafo completo no dirigido
    // Una vez se ha creado dicho grafo comienzas a realizar pruebas entre nodos àra ver si se cumple o no el test de independencia
    //  - Si dicho test devuelve un valor < p_valor (0.05) entonces podemos decir que las variables son independenites
    //     Eliminamos todos los arcos cuyas variables son condicionalmente independientes.
    // - En caso contrario no.
    //---------------------------------------
    // Despues aplicamos un algoritmo Hill-climbing con los operadores de:
    // - Arc reversal.
    // - Arc deletion
    // - Arc addition -> Caso especial, ya que se encuentra limitado a los arcos establecidos por el grafo no dirigido devuelto por el constraint-based.
    //


    @Override
    public LearningResult<DiscreteBayesNet> learnModel(DiscreteBayesNet seedNet, DiscreteData data, DiscreteParameterLearning parameterLearning) {



        return null;
    }
}
