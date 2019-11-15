package com.example.paetz.yacguide.map.cluster

import android.os.AsyncTask


class ClusterTask<T : GeoItem>(
        private val clusterManager: ClusterManager<T>
) : AsyncTask<Boolean, Void, Unit>() {

    override fun doInBackground(vararg params: Boolean?) {
        // If the map is moved without zoom-change: Add unclustered items.
        if (params[0]!!) {
            clusterManager.addLeftItems()
        } else {
            synchronized(clusterManager.clusters) {
                for (mCluster in clusterManager.clusters) {
                    synchronized(clusterManager.leftItems) {
                        clusterManager.leftItems.addAll(mCluster.items)
                    }
                    mCluster.clear()
                }
            }
            synchronized(clusterManager.clusters) {
                clusterManager.clusters.clear()
            }
            if (!isCancelled) {
                synchronized(clusterManager.clusters) {
                    if (clusterManager.clusters.size != 0) {
                        throw IllegalArgumentException()
                    }
                }
                clusterManager.addLeftItems()
            }
        }
        return
    }

    override fun onPostExecute(result: Unit) {
        clusterManager.isClustering = false
        clusterManager.redraw()
    }
}

