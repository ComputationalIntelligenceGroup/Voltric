package voltric.variables;

/**
 * Enumerates the number of space types available to the getVariables.
 */
// TODO: Por el momento no existe distinción en el tipo de variable y su stateSpace, por lo que podriamos utilizar ese
// enum como una alternativa a llamar a "instanceof" pero en un futuro puede que pasen a significar cosas diferentes.
public enum StateSpaceType {
    FINITE,
    REAL
}
