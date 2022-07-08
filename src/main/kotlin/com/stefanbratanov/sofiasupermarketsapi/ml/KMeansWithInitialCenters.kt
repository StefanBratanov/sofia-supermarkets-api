package com.stefanbratanov.sofiasupermarketsapi.ml

import com.stefanbratanov.sofiasupermarketsapi.pdf.TextWithCoordinates
import org.apache.commons.math3.exception.ConvergenceException
import org.apache.commons.math3.exception.NumberIsTooSmallException
import org.apache.commons.math3.exception.util.LocalizedFormats
import org.apache.commons.math3.ml.clustering.CentroidCluster
import org.apache.commons.math3.ml.clustering.Cluster
import org.apache.commons.math3.ml.clustering.Clusterable
import org.apache.commons.math3.ml.clustering.DoublePoint
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer
import org.apache.commons.math3.ml.distance.DistanceMeasure
import org.apache.commons.math3.random.JDKRandomGenerator
import org.apache.commons.math3.stat.descriptive.moment.Variance
import org.apache.commons.math3.util.MathUtils

/** copied from
 * @see org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer
 * only difference is that we can define our own initial centers
 */
class KMeansWithInitialCenters(
    k: Int,
    maxIterations: Int,
    measure: DistanceMeasure,
    private var initialCenters: List<CentroidCluster<TextWithCoordinates>>
) : KMeansPlusPlusClusterer<TextWithCoordinates>(k, maxIterations, measure) {

    private val random = JDKRandomGenerator()

    override fun cluster(points: MutableCollection<TextWithCoordinates>?): List<CentroidCluster<TextWithCoordinates>> {
        // sanity checks
        // sanity checks
        MathUtils.checkNotNull(points)

        // number of clusters has to be smaller or equal the number of data points
        // number of clusters has to be smaller or equal the number of data points
        if (points!!.size < k) {
            throw NumberIsTooSmallException(points.size, k, false)
        }

        // create the initial clusters

        // create the initial clusters
        var clusters = initialCenters

        // create an array containing the latest assignment of a point to a cluster
        // no need to initialize the array, as it will be filled with the first assignment

        // create an array containing the latest assignment of a point to a cluster
        // no need to initialize the array, as it will be filled with the first assignment
        val assignments = IntArray(points.size)
        assignPointsToClusters(clusters, points, assignments)

        // iterate through updating the centers until we're done

        // iterate through updating the centers until we're done
        val max = if (maxIterations < 0) Int.MAX_VALUE else maxIterations
        for (count in 0 until max) {
            var emptyCluster = false
            val newClusters: MutableList<CentroidCluster<TextWithCoordinates>> =
                ArrayList<CentroidCluster<TextWithCoordinates>>()
            for (cluster in clusters) {
                val newCenter: Clusterable
                if (cluster.points.isEmpty()) {
                    newCenter = getPointFromLargestVarianceCluster(clusters)
                    emptyCluster = true
                } else {
                    newCenter = centroidOf(cluster.points, cluster.center.point.size)
                }
                newClusters.add(CentroidCluster<TextWithCoordinates>(newCenter))
            }
            val changes = assignPointsToClusters(newClusters, points, assignments)
            clusters = newClusters

            // if there were no more changes in the point-to-cluster assignment
            // and there are no empty clusters left, return the current clusters
            if (changes == 0 && !emptyCluster) {
                return clusters
            }
        }
        return clusters
    }

    private fun assignPointsToClusters(
        clusters: List<CentroidCluster<TextWithCoordinates>>,
        points: Collection<TextWithCoordinates>,
        assignments: IntArray
    ): Int {
        var assignedDifferently = 0
        for ((pointIndex, p) in points.withIndex()) {
            val clusterIndex = getNearestCluster(clusters, p)
            if (clusterIndex != assignments[pointIndex]) {
                assignedDifferently++
            }
            val cluster: CentroidCluster<TextWithCoordinates> = clusters[clusterIndex]
            cluster.addPoint(p)
            assignments[pointIndex] = clusterIndex
        }
        return assignedDifferently
    }

    @Throws(ConvergenceException::class)
    private fun getPointFromLargestVarianceCluster(clusters: Collection<CentroidCluster<TextWithCoordinates>>): Clusterable {
        var maxVariance = Double.NEGATIVE_INFINITY
        var selected: Cluster<TextWithCoordinates>? = null
        for (cluster in clusters) {
            if (cluster.points.isNotEmpty()) {
                // compute the distance variance of the current cluster
                val center = cluster.center
                val stat = Variance()
                for (point in cluster.points) {
                    stat.increment(distance(point, center))
                }
                val variance = stat.result

                // select the cluster with the largest variance
                if (variance > maxVariance) {
                    maxVariance = variance
                    selected = cluster
                }
            }
        }

        // did we find at least one non-empty cluster ?
        if (selected == null) {
            throw ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS)
        }

        // extract a random point from the cluster
        val selectedPoints: MutableList<TextWithCoordinates> = selected.points
        return selectedPoints.removeAt(random.nextInt(selectedPoints.size))
    }

    private fun getNearestCluster(
        clusters: Collection<CentroidCluster<TextWithCoordinates>>,
        point: TextWithCoordinates
    ): Int {
        var minDistance = Double.MAX_VALUE
        var minCluster = 0
        for ((clusterIndex, c) in clusters.withIndex()) {
            val distance = distance(point, c.center)
            if (distance < minDistance) {
                minDistance = distance
                minCluster = clusterIndex
            }
        }
        return minCluster
    }

    private fun centroidOf(
        points: Collection<TextWithCoordinates>,
        dimension: Int
    ): Clusterable {
        val centroid = DoubleArray(dimension)
        for (p in points) {
            val point: DoubleArray = p.point
            for (i in centroid.indices) {
                centroid[i] += point[i]
            }
        }
        for (i in centroid.indices) {
            centroid[i] = centroid[i] / points.size
        }
        return DoublePoint(centroid)
    }
}
