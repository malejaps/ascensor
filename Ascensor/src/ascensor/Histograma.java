/*
Proyecto simulador ascensor.
Integrantes:
Maria Alejandra Pabon Salazar 1310263
Mayerly Suarez Ordoñez        1310284
 */

/*Proposito: Generador de Graficos*/

package ascensor;

import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;

public class Histograma {

    public IntervalXYDataset crearDataset(double[] valores, int numClases, String nombre) {
        HistogramDataset dataset= new HistogramDataset();
        dataset.addSeries(nombre, valores, numClases);
        return dataset;
    }

    private JFreeChart crearChart(IntervalXYDataset dataset) {
        JFreeChart chart = ChartFactory.createHistogram(
                "",
                null,
                null,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        XYPlot plot = (XYPlot) chart.getPlot();
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);

        return chart;
    }

    public JPanel crearPanel(IntervalXYDataset dataset) {
        JFreeChart chart = crearChart(dataset);
        return new ChartPanel(chart);
    }
}
