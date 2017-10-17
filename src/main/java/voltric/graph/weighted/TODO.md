La dicotomia que tengo ahora con los grafos con pesos es que al ser composite, tienen muchos metodos
que deberian ser expuestos con una fachada, de forma que se mantenga exactamente la misma funcionalidad
pero a la vez añada nuevos metodos especificos de grafos con pesos.

En el caso de que haya mucho entrelazamiento o pocos metodos que no requieran peso utiles (especificos), 
entoncessi que consideraria eliminarlos y hacer la jerarquia con pesos incluidos

Otra razon por la cual eliminarlo seria si los weighted me obligan a repetir la jerarquia entera solo por un poco de 
funcionalidad extra (vease por ejemplo hace runa version Weighted para el DAG y similares)

----------------------------------

Dado que no se puede herencia multiple, nos quedan unas clases muy similares, con gran parte repetido

Traits de Scala arreglarian esto, pero bueno, realmente no es mucho codigo, asi que "da igual". 