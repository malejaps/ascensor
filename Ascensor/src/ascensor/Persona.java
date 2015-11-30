/*
Proyecto simulador ascensor.
Integrantes:
Maria Alejandra Pabon Salazar 1310263
Mayerly Suarez Ordoñez        1310284
 */
package ascensor;

/*
 Proposito: Objeto para almacenar la informacion de llegada de personas que contiene
 reloj y pisoDestino solicitado, para ser almacenados en un ArrayList conformando colas para cada pisoDestino
 
 */
public class Persona {
    /*El momento de llegada de la persona que se obtiene sumando el tiempo de la ultima 
    llegada de persona sumado al tiempo entre llegada aleatorio (En segundos)*/
    private int reloj;
    private int pisoDestino;

    public Persona( int reloj, int piso) {
        //se podria inicializar aca la persona sumando llegada ultima persona con 
        //timepo entre llegada (aletorio) y llamando a clase generar aleatorio para piso solicitado
        this.pisoDestino=piso;
        this.reloj=reloj;
    }
//Metodos get y set para obtener los valores de las variables definidas anteriormente
    public int getReloj() {
        return reloj;
    }

    public int getPisoDestino() {
        return pisoDestino;
    }
    
}