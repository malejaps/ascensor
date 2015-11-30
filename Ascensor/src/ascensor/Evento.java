/*
Proyecto simulador ascensor.
Integrantes:
Maria Alejandra Pabon Salazar 1310263
Mayerly Suarez Ordoñez        1310284
 */


package ascensor;

/*
Proposito:Objeto para almacenar las caracteristicas(tipo de Evento, reloj y piso)
de cada suceso (evento) que sera almacenado en la LEF
 */
public class Evento {
    //Variable para identificar el evento que se produce, puede tomar los siguientes valores:
    //"paradaPisoAscensor", "llegadaPersona" y "arranqueAscensor"
    String tipoEvento;
    //Variable que almacena el cuando(hora) sucedera el evento (En segundos)
    int reloj;
    //Piso en el que esta sucediendo el evento
    int piso;

    
    //Constructor para inicializar los atributos
    public Evento(String tipoEvento, int reloj, int piso) {
        this.tipoEvento = tipoEvento;
        this.reloj = reloj;
        this.piso = piso;

    }
    
    //Metodos get y set para cada atributo definido anteriormente
    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public int getReloj() {
        return reloj;
    }

    public void setReloj(int reloj) {
        this.reloj = reloj;
    }

    public int getPiso() {
        return piso;
    }

    public void setPiso(int piso) {
        this.piso = piso;
    }

}
