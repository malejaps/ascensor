/*
 Proyecto simulador ascensor.
 Integrantes:
 Maria Alejandra Pabon Salazar 1310263
 Mayerly Suarez Ordoñez        1310284
 */

/*
 *Proposito: Simular un ascensor. Realizar el main de simulaciones y calcular las variable de desempeño.
 */
package ascensor;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.IntervalXYDataset;

public class Simulador extends javax.swing.JFrame {

    /*Variables globales*/
    int reloj;//tiempo actual
    String tipoEvento;
    int tiempoSimulacion; //tiempo total en el que se va a simular (se toma de interfaz
    int numeroSimulacion;
    //int simulaciones;   
    ArrayList<Evento> LEF;
    GeneradorAleatorios generador;
    int cantidadSimulaciones;
    ArrayList<Double> listageneradorExponencial;
    ArrayList<Evento> listageneradorPiso;
    int solicitudesPiso[];//almacena las solicitudes del ascensor realizadas en cada piso. Cada posicion es un piso.
    int pisosBajarse[];//almacena las personas que se van a bajar por cada piso. Cada posicion es un piso.
    ArrayList<Persona>[] colasAfueraPisos;// mejorar la consulta y modificacion//almacena las colas de cada piso. Cada posicion corresponde a la cola de cada piso.
    int direccion;//direccion del ascensor: -1, 0, 1     
    double tiempoEntreLlegadas;
    Ascensor ascensor;

    /*Variables de desempeño */
    //Tiempo promedio de espera 
    double tiempoEsperaPersona;
    ArrayList<Double> listaTiemposEsperaPersona;
    double acumespera;//acumula los tiempos de espera
    double tiempoPromedioEsperaPersonas;
    ArrayList<Double> listaEsperaProm;

    //Tamaño promedio de la cola
    double sumaTamanioColasPiso;
    double inicola;//inicia un cambio de cola
    double acumcola;//acumula para integrar la var. aleatoria cola
    double parejacomcola[];//auxiliar de compcola
    ArrayList compcola;//graficar comportamiento de cola
    ArrayList<Double> listaColaAfueraProm;

    //Porcentaje de personas atendidas entre las que solicitan ascensor
    //Porcentajes de personas atendidas en una simulacion de un ascensor
    double atendidos;
    double totalAtendidosSimulaciones;
    double porcentajePersonasAtendidas;
    ArrayList<Double> listaAtendidos;
    ArrayList<Double> listaPorcetanjeAtendidos;

    //Capacidad ocupada promedio
    double capacidadOcupada;
    double inioc;//inicia cambio de capacidad ocupada
    double acumocup;//acumular tiempo ocupado
    double promedioCapacidadoOcupada;
    ArrayList<Double> listaCapacidadoOcupadaprom;

    ArrayList<Double> listaGeneradorExponencialPisoUno;
    ArrayList<Double> listaGeneradorExponencialOtrosPisos;
    ArrayList<Integer> listaGeneradorPisos;

    /*Constructor*/
    @SuppressWarnings("empty-statement")
    public Simulador() {

        initComponents();
        this.tipoEvento = "";
        this.generador = new GeneradorAleatorios();

    }

    private void agregaraLEF(Evento evento) {
        //LLenar LEF por orden de ocurrencia
        for (int i = 0; i < LEF.size(); i++) {
            if (evento.getReloj() < LEF.get(i).getReloj()) {
                LEF.add(i, evento);
                return;
            }
        }
        LEF.add(evento);
    }

    private void generarLlegadaPersona(String evento, int tiempoLlegadaPersona, int piso) {
        Evento LlegadaPersona = new Evento(evento, tiempoLlegadaPersona, piso);
        agregaraLEF(LlegadaPersona);

    }

    private void generarParadaPisoAscensor(String evento, int reloj, int proximoPiso) {
        Evento parada = new Evento(evento, reloj, proximoPiso);
        agregaraLEF(parada);
    }

    private void generarArranqueAscensor(String evento, int reloj, int proximoPiso) {
        Evento arranque = new Evento(evento, reloj, proximoPiso);
        agregaraLEF(arranque);
    }

    private void eventoLlegadaPersona(Evento evento) {
        int pisoActual = evento.getPiso();
        int tiempoLlegadaProximaPersona;
        double lambdaPiso1 = 0.04;
        double lambdaDemasPisos = 0.02;
        if (pisoActual == 0) {
            tiempoEntreLlegadas = generador.generarConDistribucionExponencial(lambdaPiso1);
            listaGeneradorExponencialPisoUno.add(tiempoEntreLlegadas);
        } else {
            tiempoEntreLlegadas = generador.generarConDistribucionExponencial(lambdaDemasPisos);
            listaGeneradorExponencialOtrosPisos.add(tiempoEntreLlegadas);
        }

        int pisoDestino = generador.generarPiso(pisoActual);
        listaGeneradorPisos.add(pisoDestino);
        Persona llegada = new Persona(reloj, pisoDestino);

        //calculo variable de desepeño tamaño promedio cola. Se hace cada vez que se modifica la cola
        acumcola += (reloj - inicola) * colasAfueraPisos[pisoActual].size();
        inicola = reloj;
        colasAfueraPisos[pisoActual].add(llegada);

        solicitudesPiso[evento.getPiso()]++;
        tiempoLlegadaProximaPersona = (int) (reloj + tiempoEntreLlegadas);
        generarLlegadaPersona("L", tiempoLlegadaProximaPersona, evento.getPiso());

    }

    /*Sucede cuando el ascensor no sabe a donde ir, pues no hay nadie que se baje ni que lo solicite en un piso.*/
    private void eventoArranqueAscensor(Evento evento) {

        int proximoPiso;
        int tiempoLlegadaAlProximoPiso = reloj + ascensor.getTiempoDesplazamiento();

        //si ha llegado alguien al piso en donde esta parado esperando solicitudes para volver a arrancar
        if (solicitudesPiso[evento.getPiso()] != 0) {

            resultados.append("Ascensor recoge a persona en este mismo piso y luego inicia su arranque hacia otros pisos.");
            resultados.append(System.getProperty("line.separator"));

            direccion = 1;
            generarParadaPisoAscensor("P", reloj, evento.getPiso());

        } else {
            //Ascensor busca solicitudes hacia arriba 

            resultados.append("Ascensor busca solicitudes hacia arriba para poder arrancar");
            resultados.append(System.getProperty("line.separator"));

            direccion = 1;
            proximoPiso = direccionSubir(evento.getPiso());

            //Si no encontro arriba solicitudes entonces cambie de direccion y busque abajo
            if (proximoPiso == -1) {

                resultados.append("Ascensor busca solicitudes hacia abajo para poder arrancar");
                resultados.append(System.getProperty("line.separator"));

                direccion = -1;
                proximoPiso = direccionBajar(evento.getPiso());

            }

            //Si no encontro arriba ni abajo solicitudes entonces arranca hasta que haya una nueva solicitud 
            if (proximoPiso == -1) {
                direccion = 0;
                generarArranqueAscensor("A", (reloj + 20), evento.getPiso());
            } else {

                resultados.append("Arranca ascensor" + " en t=" + reloj + "seg " + " desde el piso " + (evento.getPiso() + 1) + " hacia el piso " + (proximoPiso + 1));
                resultados.append(System.getProperty("line.separator"));

                generarParadaPisoAscensor("P", tiempoLlegadaAlProximoPiso, proximoPiso);
            }
        }

    }

    private void eventoParadaPisoAscensor(Evento evento) {

        int pisoActual = evento.getPiso();
        int proximoPiso = -1;
        int tiempoLlegadaProximoPiso = 0;
        int sumaTiemposEntradaPersona = 0;
        int sumaTiemposSalidaPersona = 0;
        int sumaSolicitudesPiso = 0;
        int sumaPisosBajarse = 0;
        int tiempoLlegadaPersona = 0;
        int cantidadPersonasBajarse = pisosBajarse[pisoActual];
        int cantidadPersonasEsperando = solicitudesPiso[pisoActual];

        //si alguien sale del ascensor en este piso
        if (cantidadPersonasBajarse != 0) {

            atendidos += cantidadPersonasBajarse;

            //Para calcular variable de desempeño capacidad ocupada promedio            
            acumocup += (reloj - inioc) * capacidadOcupada * cantidadPersonasBajarse;
            capacidadOcupada -= cantidadPersonasBajarse;
            inioc = reloj;

            sumaTiemposSalidaPersona = cantidadPersonasBajarse;
            pisosBajarse[pisoActual] = 0;
            tiempoLlegadaProximoPiso += sumaTiemposSalidaPersona;

            resultados.append("Salen personas:"
                    + "\nCantidad de personas a bajarse en este piso " + cantidadPersonasBajarse
                    + "\nCapacidad maxima del ascensor " + ascensor.getCapacidadMaxima()
                    + "\nCapacidad ocupada en el ascensor " + capacidadOcupada);
            resultados.append(System.getProperty("line.separator"));

        }

        //si alguien entra al ascensor en este piso	
        int capacidadDisponible = (ascensor.getCapacidadMaxima()) - (int) capacidadOcupada;

        resultados.append("Capacidad disponible " + capacidadDisponible);
        resultados.append(System.getProperty("line.separator"));
        int cantidadSubirse = 0;
        if ((cantidadPersonasEsperando != 0) && (capacidadDisponible != 0)) {
            for (int i = 0; i < capacidadDisponible; i++) {

                if (!colasAfueraPisos[pisoActual].isEmpty()) {

                    cantidadSubirse++;
                    tiempoLlegadaPersona = colasAfueraPisos[pisoActual].get(0).getReloj();
                    tiempoEsperaPersona = reloj - tiempoLlegadaPersona;
                    listaTiemposEsperaPersona.add(tiempoEsperaPersona);

                    //Para calcular variable de desempeño tamaño promedio cola. Se hace cada vez que se modifica la cola
                    acumcola += (reloj - inicola) * colasAfueraPisos[pisoActual].size();
                    inicola = reloj;
                    parejacomcola[0] = reloj;
                    parejacomcola[1] = colasAfueraPisos[pisoActual].size();
                    compcola.add(parejacomcola);

                    pisosBajarse[colasAfueraPisos[pisoActual].get(0).getPisoDestino()]++;

                    colasAfueraPisos[pisoActual].remove(0);

                    acumocup += (reloj - inioc) * capacidadOcupada;
                    inioc = reloj;
                    capacidadOcupada++;

                    solicitudesPiso[pisoActual]--;

                }

            }
            //Para calcular variable de desempeño capacidad ocupada promedio   
            sumaTiemposEntradaPersona = cantidadSubirse;
            tiempoLlegadaProximoPiso += sumaTiemposEntradaPersona;

            resultados.append("Entran Personas:" + "\nCantidad de personas subirse en este piso " + cantidadSubirse + "\nCantidad Personas esperando en el piso " + cantidadPersonasEsperando
                    + "\nCantidad Personas que se quedan esperando en este piso " + solicitudesPiso[pisoActual]
                    + "\nCapacidad maxima del ascensor " + ascensor.getCapacidadMaxima()
                    + "\nCapacidad ocupada en el ascensor " + capacidadOcupada);
            resultados.append(System.getProperty("line.separator"));

        }

        //Si alguien se sube o alguien se baja,abrio puertas, entonces calcule la llegada al proximo piso teniendo en cuenta esto
        if (tiempoLlegadaProximoPiso != 0) {
            tiempoLlegadaProximoPiso += reloj + ascensor.getTiempoArranque() + ascensor.getTiempoDesplazamiento();
        } else {//pero si nadie se sube ni nadie se baja, no abre puertas, entonces calcule la llegada al proximo piso teniendo en cuenta esto
            tiempoLlegadaProximoPiso += reloj + ascensor.getTiempoDesplazamiento();
        }


        /*Cuadrar proxima direccion y proximo piso del ascensor*/
        for (int i = 0; i < solicitudesPiso.length; i++) {
            sumaSolicitudesPiso += solicitudesPiso[i];
        }
        for (int i = 0; i < pisosBajarse.length; i++) {
            sumaPisosBajarse += pisosBajarse[i];
        }

        resultados.append("Total solicitudes en pisos: " + sumaSolicitudesPiso + "\nTotal personas que se desean bajar: " + sumaPisosBajarse);
        resultados.append(System.getProperty("line.separator"));

        if ((sumaSolicitudesPiso == 0) && (sumaPisosBajarse == 0)) {
            direccion = 0;
            resultados.append("Ascensor quedo parado y tiene direccion 0 -> Ascensor necesita arrancar ");
            resultados.append(System.getProperty("line.separator"));

            generarArranqueAscensor("A", (reloj + 20), pisoActual);

        } else {
            switch (direccion) {

                case 1:
                    proximoPiso = direccionSubir(pisoActual);
                    if (proximoPiso == -1) {
                        direccion = -1;
                        proximoPiso = direccionBajar(pisoActual);
                    }
                    break;

                case -1:
                    proximoPiso = direccionBajar(pisoActual);
                    if (proximoPiso == -1) {
                        direccion = -1;
                        proximoPiso = direccionSubir(pisoActual);
                    }
                    break;
            }
            generarParadaPisoAscensor("P", tiempoLlegadaProximoPiso, proximoPiso);
        }

    }

    private int direccionBajar(int pisoActual) {
        int proximoPiso = -1;
        int sgtePiso;
        for (int i = pisoActual; i > 0; i--) {
            sgtePiso = i - 1;

            resultados.append(solicitudesPiso[sgtePiso] + " solicitudes en el piso " + (sgtePiso + 1));
            resultados.append(System.getProperty("line.separator"));
            //si alguien quiere entrar o salir en pisos de abajo
            if ((solicitudesPiso[sgtePiso] != 0) || (pisosBajarse[sgtePiso] != 0)) {
                proximoPiso = sgtePiso;
                direccion = -1;
                i = 0;//pare de buscar;
            }
        }
        return proximoPiso;
    }

    private int direccionSubir(int pisoActual) {
        int proximoPiso = -1;
        int sgtePiso;

        for (int i = pisoActual; i < 5; i++) {
            sgtePiso = i + 1;

            resultados.append(solicitudesPiso[sgtePiso] + " solicitudes en el piso " + (sgtePiso + 1));
            resultados.append(System.getProperty("line.separator"));
            //si alguien quiere entrar o salir en pisos de arriba
            if ((solicitudesPiso[sgtePiso] != 0) || (pisosBajarse[sgtePiso] != 0)) {
                proximoPiso = sgtePiso;
                direccion = 1;
                i = 5;//pare de buscar;
            }
        }
        return proximoPiso;
    }


    /*Inicializar una simulacion*/
    private void inicializar(Ascensor ascensor) {
        this.ascensor = ascensor;

        LEF = new ArrayList<Evento>();
        direccion = 1;//inicialmente direccion es subiendo

        colasAfueraPisos = new ArrayList[6];
        colasAfueraPisos[0] = new ArrayList<Persona>();
        colasAfueraPisos[1] = new ArrayList<Persona>();
        colasAfueraPisos[2] = new ArrayList<Persona>();
        colasAfueraPisos[3] = new ArrayList<Persona>();
        colasAfueraPisos[4] = new ArrayList<Persona>();
        colasAfueraPisos[5] = new ArrayList<Persona>();
        solicitudesPiso = new int[6];
        pisosBajarse = new int[6];

        tiempoEntreLlegadas = 0;
        reloj = 0;

        /*Tiempo promedio de espera */
        tiempoEsperaPersona = 0;
        acumespera = 0;
        listaTiemposEsperaPersona = new ArrayList<Double>();
        tiempoPromedioEsperaPersonas = 0;

        /*Tamaño promedio de la cola*/
        sumaTamanioColasPiso = 0;
        inicola = 0;
        acumcola = 0;
        parejacomcola = new double[2];
        compcola = new ArrayList();
        parejacomcola[0] = 0;
        parejacomcola[1] = 0;
        compcola.add(parejacomcola);

        /*Porcentaje de personas atendidas entre las que solicitan ascensor*/
        atendidos = 0;
        porcentajePersonasAtendidas = 0;

        /*Capacidad ocupada promedio */
        capacidadOcupada = 0;
        inioc = 0;
        acumocup = 0;

        promedioCapacidadoOcupada = 0;


        /*Eventos iniciales con los que empieza la simulacion*/
        
         //Generar llegadas a cada piso menos en los pisos 5 y 6 para iniciar con un arranque. Generar una parada del ascensor.
         generarLlegadaPersona("L", 0, 0);
         generarLlegadaPersona("L", 2, 1);
         generarLlegadaPersona("L", 5, 2);
         generarLlegadaPersona("L", 4, 3);
         generarLlegadaPersona("L", 3, 4);
         generarLlegadaPersona("L", 5, 5);         
         generarParadaPisoAscensor("P", 2, 3);
        



        /*Mostrar LEF en reloj=ini*/
        for (int i = 0; i < LEF.size(); i++) {

            resultados.append("LEF ini: " + "evento: " + LEF.get(i).getTipoEvento() + " t=" + LEF.get(i).getReloj() + " Piso: " + (LEF.get(i).getPiso() + 1));
            resultados.append(System.getProperty("line.separator"));
        }

    }

    /*Finalizar una simulacion. Se calculan las variables de desempeño de cada simulacion*/
    private void finalizar() {

        //Variable desempeño:porcentaje personas atendidas. Calculo continua cuando se finalizan todas las simulaciones
        listaAtendidos.add(atendidos);

        //Variable desempeño:tamaño promedio cola
        for (int i = 0; i < colasAfueraPisos.length; i++) {
            sumaTamanioColasPiso += colasAfueraPisos[i].size();
        }
        acumcola += (reloj - inicola) * sumaTamanioColasPiso;
        listaColaAfueraProm.add(acumcola / reloj);

        //Variable desempeño:tiempo promedio de espera         
        for (int i = 0; i < listaTiemposEsperaPersona.size(); i++) {
            acumespera += listaTiemposEsperaPersona.get(i);
        }
        listaEsperaProm.add(acumespera / listaTiemposEsperaPersona.size());

        acumocup += (reloj - inioc) * capacidadOcupada;
        listaCapacidadoOcupadaprom.add(acumocup / reloj);

    }

    private void simularUnaVez(Ascensor ascensor) {
        inicializar(ascensor);
        Evento eventoActual;
        tiempoSimulacion = 0;

        /*Simular hasta 8 horas*/
        while (tiempoSimulacion < 28800) {
            eventoActual = LEF.get(0);
            reloj = eventoActual.getReloj();
            tipoEvento = eventoActual.getTipoEvento();

            switch (eventoActual.getTipoEvento()) {

                case "L":

                    resultados.append("Evento--->Persona llega " + " en t=" + reloj + "seg." + " al piso " + (eventoActual.getPiso() + 1));
                    resultados.append(System.getProperty("line.separator"));
                    eventoLlegadaPersona(eventoActual);
                    break;
                case "P":

                    resultados.append("Evento--->Ascensor para en el piso " + (eventoActual.getPiso() + 1) + " en t=" + reloj + "seg.");
                    resultados.append(System.getProperty("line.separator"));

                    eventoParadaPisoAscensor(eventoActual);
                    break;
                case "A":

                    resultados.append("Evento--->Ascensor inicia un arranque desde el piso " + (eventoActual.getPiso() + 1) + " en t=" + reloj + "seg.");
                    resultados.append(System.getProperty("line.separator"));
                    eventoArranqueAscensor(eventoActual);
                    break;
            }

            LEF.remove(0);
            tiempoSimulacion = reloj;

        }

        //Calcula variables de desempeño
        finalizar();

    }

    //repetir las simulaciones varias veces 
    private void simularVariasVeces(Ascensor ascensor, int numeroSimulaciones) {

        numeroSimulacion = 0;
        listaCapacidadoOcupadaprom = new ArrayList<Double>();
        listaEsperaProm = new ArrayList<Double>();
        listaColaAfueraProm = new ArrayList<Double>();
        listaPorcetanjeAtendidos = new ArrayList<Double>();
        listaAtendidos = new ArrayList<Double>();

        listaGeneradorExponencialPisoUno = new ArrayList<Double>();
        listaGeneradorExponencialOtrosPisos = new ArrayList<Double>();
        listaGeneradorPisos = new ArrayList<Integer>();

        while (numeroSimulacion < numeroSimulaciones) {
      

            resultados.append("****************************************************Inicia una simulacion " +numeroSimulacion+"****************************************************************");
            resultados.append(System.getProperty("line.separator"));
            simularUnaVez(ascensor);
            numeroSimulacion++;
        }

        //Para calcular porcentaje de personas atendidas en una simulacion
        for (int i = 0; i < listaAtendidos.size(); i++) {
            totalAtendidosSimulaciones += listaAtendidos.get(i);
        }
        for (int i = 0; i < listaAtendidos.size(); i++) {
            porcentajePersonasAtendidas = (listaAtendidos.get(i) * 100) / totalAtendidosSimulaciones;
            listaPorcetanjeAtendidos.add(porcentajePersonasAtendidas);
            
        }


        /*Evaluar variables de desempeño*/
        resultados.append("\n" + "**EVALUACION VARIABLES DE DESEMPEÑO**");
        resultados.append(System.getProperty("line.separator"));

        resultados.append("**Capacidad ocupada promedio**");
        resultados.append(System.getProperty("line.separator"));
        evaluarLista(listaCapacidadoOcupadaprom);

        resultados.append("**Tiempo promedio espera**");
        resultados.append(System.getProperty("line.separator"));
        evaluarLista(listaEsperaProm);

        resultados.append("**Tamanio promedio cola de afuera**");
        resultados.append(System.getProperty("line.separator"));
        evaluarLista(listaColaAfueraProm);

        resultados.append("**Porcentaje de personas atendidas**");
        resultados.append(System.getProperty("line.separator"));
        evaluarLista(listaPorcetanjeAtendidos);

    }

    /*Evaluacion de variables de desempeño*/
    private void evaluarLista(ArrayList<Double> lista) {
        double promedio;
        double desviacionEstantar;
        double inicioIntervaloConfianza;
        double finIntervaloConfianza;

        resultados.append("Datos { ");

        for (int i = 0; i < lista.size(); i++) {

            resultados.append(lista.get(i) + ",");

        }

        resultados.append("} ");
        resultados.append(System.getProperty("line.separator"));
        promedio = sumarLista(lista) / lista.size();

        resultados.append("Promedio: " + promedio);
        resultados.append(System.getProperty("line.separator"));
        desviacionEstantar = Math.sqrt(sumatoria(lista, promedio) / (lista.size() - 1));

        resultados.append("Desviacion Estandar: " + desviacionEstantar);
        resultados.append(System.getProperty("line.separator"));
        inicioIntervaloConfianza = promedio - 1.96 * desviacionEstantar / Math.sqrt(lista.size());
        finIntervaloConfianza = promedio + 1.96 * desviacionEstantar / Math.sqrt(lista.size());

        resultados.append("Intervalo de confianza (Alpha=0.05): [" + inicioIntervaloConfianza + "," + finIntervaloConfianza + "]");
        resultados.append(System.getProperty("line.separator"));

    }

    /*Auxiliar de evaluarLista(Lista)*/
    private double sumatoria(ArrayList<Double> lista, double promedio) {
        double suma = 0;
        for (int i = 0; i < lista.size(); i++) {
            suma += Math.pow(lista.get(i) - promedio, 2);
        }
        return suma;
    }

    /*Auxiliar de evaluarLista(Lista)*/
    private double sumarLista(ArrayList<Double> lista) {
        double suma = 0;
        for (int i = 0; i < lista.size(); i++) {
            suma += (double) lista.get(i);
        }
        return suma;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        seleccionar = new javax.swing.JButton();
        seleccionarSimulaciones = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        campoVelocidad = new javax.swing.JTextField();
        campoTiempoArranque = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        seleccionarAscensor = new javax.swing.JComboBox();
        campoCapacidad = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        seleccionar1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultados = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Simulacion ascensor");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setResizable(false);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        jLayeredPane1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        jLabel5.setText("Desplazamiento entre pisos");
        jLayeredPane1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 220, 30));

        jLabel4.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        jLabel4.setText("Tiempo Arranque");
        jLayeredPane1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 90, 150, -1));

        jLabel3.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        jLabel3.setText("Capacidad");
        jLayeredPane1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 90, 80, -1));

        jLabel7.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        jLabel7.setText("Cantidad de simulaciones");
        jLayeredPane1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 50, 210, 20));

        seleccionar.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        seleccionar.setText("Simular");
        seleccionar.setToolTipText("Empezar simulaciones del ascensor");
        seleccionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seleccionarActionPerformed(evt);
            }
        });
        jLayeredPane1.add(seleccionar, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 130, 120, 30));

        seleccionarSimulaciones.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
        seleccionarSimulaciones.setToolTipText("Seleccione la cantidad de simulaciones");
        seleccionarSimulaciones.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                seleccionarSimulacionesItemStateChanged(evt);
            }
        });
        seleccionarSimulaciones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seleccionarSimulacionesActionPerformed(evt);
            }
        });
        jLayeredPane1.add(seleccionarSimulaciones, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 50, 60, -1));

        jLabel8.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        jLabel8.setText(" Ascensor");
        jLayeredPane1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 50, 80, -1));

        campoVelocidad.setEditable(false);
        campoVelocidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoVelocidadActionPerformed(evt);
            }
        });
        jLayeredPane1.add(campoVelocidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 90, 40, 23));

        campoTiempoArranque.setEditable(false);
        campoTiempoArranque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoTiempoArranqueActionPerformed(evt);
            }
        });
        jLayeredPane1.add(campoTiempoArranque, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 90, 40, 24));

        jLabel1.setFont(new java.awt.Font("Corbel", 1, 24)); // NOI18N
        jLabel1.setText("Simulacion Ascensor");
        jLayeredPane1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 10, -1, -1));

        seleccionarAscensor.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-------------", "Ascensor 1", "Ascensor 2", "Ascensor 3", "Ascensor 4" }));
        seleccionarAscensor.setToolTipText("Seleccione un ascensor");
        seleccionarAscensor.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                seleccionarAscensorItemStateChanged(evt);
            }
        });
        seleccionarAscensor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seleccionarAscensorActionPerformed(evt);
            }
        });
        jLayeredPane1.add(seleccionarAscensor, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 97, -1));

        campoCapacidad.setEditable(false);
        campoCapacidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoCapacidadActionPerformed(evt);
            }
        });
        jLayeredPane1.add(campoCapacidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 90, 24, 23));
        jLayeredPane1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 670, 10));

        seleccionar1.setFont(new java.awt.Font("Corbel", 0, 18)); // NOI18N
        seleccionar1.setText("Graficas generadores");
        seleccionar1.setToolTipText("Empezar simulaciones del ascensor");
        seleccionar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seleccionar1ActionPerformed(evt);
            }
        });
        jLayeredPane1.add(seleccionar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 130, 200, 30));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
        );

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        resultados.setEditable(false);
        resultados.setColumns(20);
        resultados.setRows(5);
        jScrollPane1.setViewportView(resultados);

        jPanel3.add(jScrollPane1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void campoVelocidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoVelocidadActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoVelocidadActionPerformed

    private void campoTiempoArranqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoTiempoArranqueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoTiempoArranqueActionPerformed

    private void seleccionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seleccionarActionPerformed
        if (seleccionarAscensor.getSelectedItem().equals("-------------")) {
            JOptionPane.showMessageDialog(null, "Seleccione un ascensor");
        } else {

            int capacidad = Integer.parseInt(campoCapacidad.getText());
            int arranque = Integer.parseInt(campoTiempoArranque.getText());
            int desplazamiento = Integer.parseInt(campoVelocidad.getText());//tiempo de Desplazamiento entre pisos
            ascensor = new Ascensor(capacidad, arranque, desplazamiento);

            cantidadSimulaciones = Integer.parseInt((String) seleccionarSimulaciones.getSelectedItem());
            //System.out.print(cantidadSimulaciones);
            //simularVariasVeces(ascensor, cantidadSimulaciones);
            ascensor = new Ascensor(capacidad, arranque, desplazamiento);
            simularVariasVeces(ascensor, cantidadSimulaciones);

            //Si numero de simulaciones es mayor a 100 no se grafica.
            if (cantidadSimulaciones <= 100) {
                graficar(listaCapacidadoOcupadaprom, "Capacidad ocupada promedio", listaEsperaProm, "Tiempo promedio espera", listaColaAfueraProm, "Tamanio promedio cola de afuera", listaPorcetanjeAtendidos, "Porcentaje de personas atendidas");
            }

        }


    }//GEN-LAST:event_seleccionarActionPerformed

    private void seleccionarAscensorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_seleccionarAscensorItemStateChanged
        // TODO add your handling code here:

        String seleccionado = (String) seleccionarAscensor.getSelectedItem();
        switch (seleccionado) {

            case "Ascensor 1":

                campoCapacidad.setText("4");
                campoTiempoArranque.setText("20");
                campoVelocidad.setText("100");
                resultados.setText("");

                break;
            case "Ascensor 2":
                campoCapacidad.setText("6");
                campoTiempoArranque.setText("20");
                campoVelocidad.setText("100");
                resultados.setText("");

                break;
            case "Ascensor 3":
                campoCapacidad.setText("4");
                campoTiempoArranque.setText("5");
                campoVelocidad.setText("30");
                resultados.setText("");

                break;
            case "Ascensor 4":
                campoCapacidad.setText("6");
                campoTiempoArranque.setText("5");
                campoVelocidad.setText("30");
                resultados.setText("");

                break;
            default:
                JOptionPane.showMessageDialog(null, "Seleccione un Ascensor");
        }


    }//GEN-LAST:event_seleccionarAscensorItemStateChanged

    private void seleccionarAscensorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seleccionarAscensorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_seleccionarAscensorActionPerformed

    private void seleccionarSimulacionesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seleccionarSimulacionesActionPerformed
        // TODO add your handling code here:

        resultados.setText("");

    }//GEN-LAST:event_seleccionarSimulacionesActionPerformed

    private void campoCapacidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoCapacidadActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoCapacidadActionPerformed

    private void seleccionar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seleccionar1ActionPerformed
        graficarGeneradores(cantidadSimulaciones, listaGeneradorExponencialPisoUno, "Generador exponencial Piso 1", listaGeneradorExponencialOtrosPisos, "Generador exponencial Otros pisos", listaGeneradorPisos, "Generador de pisos");
    }//GEN-LAST:event_seleccionar1ActionPerformed

    private void seleccionarSimulacionesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_seleccionarSimulacionesItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_seleccionarSimulacionesItemStateChanged

    public static void main(String args[]) {

        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Simulador.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Simulador.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Simulador.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Simulador.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new Simulador().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField campoCapacidad;
    private javax.swing.JTextField campoTiempoArranque;
    private javax.swing.JTextField campoVelocidad;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextArea resultados;
    private javax.swing.JButton seleccionar;
    private javax.swing.JButton seleccionar1;
    private javax.swing.JComboBox seleccionarAscensor;
    private javax.swing.JComboBox seleccionarSimulaciones;
    // End of variables declaration//GEN-END:variables

    //Convertir un arraylist a un arreglo
    public double[] llenarArreglo(ArrayList arreglo) {
        double arreglolleno[] = new double[arreglo.size()];

        for (int i = 0; i < arreglo.size(); i++) {

            arreglolleno[i] = Double.parseDouble("" + arreglo.get(i));
        }

        return arreglolleno;
    }

//Graficar numeros generados con generadores aleatorios
    private void graficarGeneradores(int simulaciones, ArrayList<Double> lista1, String nombreLista1, ArrayList<Double> lista2, String nombreLista2, ArrayList lista3, String nombreLista3) {
        if ((listaCapacidadoOcupadaprom == null) || (listaColaAfueraProm == null) || (listaEsperaProm == null) || (listaPorcetanjeAtendidos == null)) {
            JOptionPane.showMessageDialog(null, "Primero debe generar una simulación para visualizar gráficos");
        } else {

            double[] arreglo1, arreglo2;
            double[] arreglo3;
            arreglo1 = llenarArreglo(lista1);
            arreglo2 = llenarArreglo(lista2);
            arreglo3 = llenarArreglo(lista3);

            Histograma hist = new Histograma();
            IntervalXYDataset dataset;

            JFrame ventana = new JFrame("Graficos generadores aleatorios");
            ventana.setLayout(new BoxLayout(ventana.getContentPane(), BoxLayout.X_AXIS));
            ventana.setLocation(250, 250);
            ventana.setMinimumSize(new Dimension(750, 390));

            dataset = hist.crearDataset(arreglo1, lista1.size(), "Generador exponencial llegadas en el piso 1");
            ventana.add(hist.crearPanel(dataset));
            dataset = hist.crearDataset(arreglo2, lista2.size(), "Generador exponencial llegadas en los otros pisos");
            ventana.add(hist.crearPanel(dataset));
            dataset = hist.crearDataset(arreglo3, lista3.size(), "Generador de pisos destinos");
            ventana.add(hist.crearPanel(dataset));

            ventana.setVisible(true);
            

        }
    }

    //Graficar variables de desempeño para cada simulacion
    private void graficar(ArrayList<Double> lista1, String nombreLista1, ArrayList<Double> lista2, String nombreLista2, ArrayList<Double> lista3, String nombreLista3, ArrayList<Double> lista4, String nombreLista4) {

        JFreeChart grafica1;
        JFreeChart grafica2;
        JFreeChart grafica3;
        JFreeChart grafica4;
        DefaultCategoryDataset datos1 = new DefaultCategoryDataset();
        DefaultCategoryDataset datos2 = new DefaultCategoryDataset();
        DefaultCategoryDataset datos3 = new DefaultCategoryDataset();
        DefaultCategoryDataset datos4 = new DefaultCategoryDataset();

        //enviar datos a la graficas
        for (int i = 0; i < lista1.size(); i++) {
            datos1.addValue((Number) (lista1.get(i)), "id", (i + 1));
            datos2.addValue((Number) (lista1.get(i)), "id", (i + 1));
            datos3.addValue((Number) (lista1.get(i)), "id", (i + 1));
            datos4.addValue((Number) (lista1.get(i)), "id", (i + 1));

        }

        //Preparar graficas
        grafica1 = ChartFactory.createBarChart("Variable de desempeño " + nombreLista1, "Numeros simulaciones", nombreLista1, datos1, PlotOrientation.VERTICAL, false, true, false);
        grafica2 = ChartFactory.createBarChart("Variable de desempeño " + nombreLista2, "Numero simulaciones", nombreLista2, datos2, PlotOrientation.VERTICAL, false, true, false);
        grafica3 = ChartFactory.createBarChart("Variable de desempeño " + nombreLista3, "Numero simulaciones", nombreLista3, datos3, PlotOrientation.VERTICAL, false, true, false);
        grafica4 = ChartFactory.createBarChart("Variable de desempeño " + nombreLista4, "Numero simulaciones", nombreLista4, datos4, PlotOrientation.VERTICAL, false, true, false);

        ChartPanel Panel1 = new ChartPanel(grafica1);
        ChartPanel Panel2 = new ChartPanel(grafica2);
        ChartPanel Panel3 = new ChartPanel(grafica3);
        ChartPanel Panel4 = new ChartPanel(grafica4);
        JFrame Ventana = new JFrame("Graficas de variables de desempeño");

        Ventana.setLayout(new GridLayout(2, 2));
        Ventana.setPreferredSize(new Dimension(900, 600));
        Ventana.add(Panel1);
        Ventana.add(Panel2);
        Ventana.add(Panel3);
        Ventana.add(Panel4);
        Ventana.setLocation(250, 250);
        Ventana.pack();
        Ventana.setVisible(true);

    }

}
