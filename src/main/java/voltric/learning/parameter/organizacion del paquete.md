Deberia haber solo dos tipos de EM, el "normal" y el local. En cada caso existira la posibilidad de ejecutarlo
de forma paralela o secuencial, pero dado que el ParallelEm es exactamente igual a nivel teorico que el Local
deberian compartir clase y ofrecerse en dos metodos separados.

Con ello el paquete quedaria con lo siguiente:

- ParameterLearning
    - DiscreteParameterLearning
    - ContinuousParameterLearning
- AbstractEM implements DiscreteParameterLearning
- EM extends AbstractEM
- LocalEM extends AbstractEM
- SVB implements DiscreteParameterLearning, ContinuousParameterLearning