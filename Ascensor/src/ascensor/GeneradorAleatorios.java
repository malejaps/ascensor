/*
 Proyecto simulador ascensor.
 Integrantes:
 Maria Alejandra Pabon Salazar 1310263
 Mayerly Suarez Ordoñez        1310284
 */

package ascensor;

import java.util.Random;

/*
Proposito:Clase utilizada en la generacion de numeros pseudoaleatorios (tipo Exponencial
para las llegadas de personas, donde es mas probable que al inicio se generen mas llegadas
y al final disminuyan las llegadas; tipo Uniforme para la generacion aleatoria de pisos destino,
donde el piso 1 tiene 4 veces mas probabilidad de ser solicitado.
 *
 */
public class GeneradorAleatorios {
    /*Variable de tipo ramdom para generar la semilla usada en la Distribucion Uniforme
    Es importante la forma de escoger la semilla pero es interesante generarla de forma 
    aleatoria para visualizar los resultados*/
    Random semilla;

    //Constructor para asignar valor a la semilla de forma pseudoaleatoria
    public GeneradorAleatorios() {
        semilla = new Random();
    }

    /*Funcion que genera un numero pseudoaleatorio con distribucion U[0,1] Uniforme
    //Funcion Auxiliar para la generacion de pseudoaleatoria de llegadas de personas
    //usada en el metodo generarConDistribucionExponencial */
    public double generarConDistribucionUniforme() {
        return semilla.nextDouble();
    }

    /*Funcion que genera un numero pseudoaleatorio con distribucion exponencial para las llegadas
    de personas en cada piso*/
    public double generarConDistribucionExponencial(double lambda) {

        //Formula para generar pseudo aleatorios de forma Exponencial
        return Math.log(generarConDistribucionUniforme()) / (-lambda);
    }

    //Funcion que genera un numero pseudoaleatorio para el piso destino de la persona que escoge en la llegada
    public int generarPiso(int pisoActual) {
        
        /*Arreglo con los pisos de 0 a 5 que representan los pisos de 1 a 6, donde 0 es equivalente a 1,para 
        simular que el piso 0 (1) sea 4 veces mas probable que los pisos de 1 a 5 (2 a 6)
        Esto se realiza porque los pisos esan representados por los indices en arreglos que inician desde
        0 */
        int[] pisos = {0, 0, 0, 0, 1, 2, 3, 4, 5};
        //generar un numero entre 0 y 8 correspondiente a la posicion del arreglo pisos[]
        int pisoGenerado=pisos[semilla.nextInt(8)];
//        Random generador = new Random();        
//        pisoGenerado = pisos[generador.nextInt(8)];
        
        //ciclo para asegurarse que el piso destino no sea igual al piso actual
        while (pisoGenerado == pisoActual) {
            pisoGenerado = pisos[semilla.nextInt(8)];
        }
        return pisoGenerado;

    }

}
