/*
Proyecto simulador ascensor.
Integrantes:
Maria Alejandra Pabon Salazar 1310263
Mayerly Suarez Ordoñez        1310284
 */


package ascensor;

/*Proposito:Objeto Ascensor utilizado para almacenar las caracteristicas capacidad, tiempo de arranque
y tiempo de desplazamiento entre pisos de cada Ascensor cuando se crean los 4 tipos de Ascensor propuestos:

Ascensor capacidad Tiempo de arranque Desplazamiento entre dos pisos
1           4              20                    100
2           6              20                    100
3           4               5                     30
4           6               5                     30*/
public class Ascensor{
    
    //Numero total de Personas que puede trasportar el ascensor
    private int capacidadMaxima;
    //Cantidad de tiempo que tarda en cerrar puertas y disponersa a arrancar(En segundos)
    private int tiempoArranque;
    //Cantidad de tiempo que tarda desplazandose entre dos pisos (En segundos)
    private int tiempoDesplazamiento;
    
    
    //Construcutor para instanciar los tres atributos de tipo entero anteriormente definidos, 
    //cuando se crea un objeto de Tipo de Ascensor
    public Ascensor(int capacidadMaxima, int tiempoArranque, int timpoDesplazamiento) {
        this.capacidadMaxima = capacidadMaxima;
        this.tiempoArranque = tiempoArranque;
        this.tiempoDesplazamiento = timpoDesplazamiento;
    }
  
    
    //Metodos get y set para cada atributo
    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public int getTiempoArranque() {
        return tiempoArranque;
    }

    public int getTiempoDesplazamiento() {
        return tiempoDesplazamiento;
    }

    public void setCapacidadMaxima(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public void setTiempoArranque(int tiempoArranque) {
        this.tiempoArranque = tiempoArranque;
    }

    public void setTiempoDesplazamiento(int tiempoDesplazamiento) {
        this.tiempoDesplazamiento = tiempoDesplazamiento;
    }
    
    
}