## . Results from benchmark

Pgbench run as a Kubernetes scheduled job within the cluster (on a distinct node from databases) with the following specs:
- Scaling factor: 1
- Query mode: simple (restricted here to Read queries)
- Number of clients: 80
- Number of threads: 8

Test cases:
- Master direct: pgbench runs directly against the master node with `nclients` concurrent clients. This would represent the 'old' setup but with replication added on which would slightly tax performance.
- Slave direct: pgbench runs directly against the slave service (with two exact endpoints). This would be the most optimal situation since pgbench clients can query both databases and thus reduce the response time on both. (Purely hypothethical as test cases only touch read-only queries)
- Single pgpool instance: pgbench runs against pgpool connected to one master node and to slaves.
- Multiple pgpool instances (3): pgbench runs against pgpool-service connected to 3 pgpool non clustered instance.

|                | Average  Latency(ms) | tps(with handshake) | tps(w/ handshake) |  Q0   |  Q1   |   Q2   |   Q3    |   Q4   |    Q5    |   Q6   |  Q7   |  Q8   |  Q9   |  Q10  |  Q11   |  Q12  |  Q13   |  Q14  |    Q15    |  Q16   |  Q17  |
|:--------------:|:--------------------:|:-------------------:|:-----------------:|:-----:|:-----:|:------:|:-------:|:------:|:--------:|:------:|:-----:|:-----:|:-----:|:-----:|:------:|:-----:|:------:|:-----:|:---------:|:------:|:-----:|
| Master  Direct |       7448.431       |      10.740517      |     10.740645     | 2.326 | 1.624 | 8.176  | 536.314 | 7.345  | 3018.764 | 21.753 | 3.847 | 2.468 | 1.650 | 3.315 | 15.557 | 2.919 | 2.341  | 3.209 | 3781.280  | 10.604 | 4.490 |
| Slave  Direct  |       6655.745       |      12.019692      |     12.019790     | 1.180 | 0.992 | 5.526  | 411.687 | 6.348  | 2746.081 | 29.709 | 1.817 | 1.172 | 0.832 | 2.999 | 24.242 | 1.463 | 0.965  | 2.770 | 3388.693  | 8.212  | 3.042 |
| Single pgpool  |       8364.560       |       9.54162       |      9.56348      | 2.727 | 2.167 | 8.458  | 575.846 | 8.354  | 3432.151 | 23.134 | 4.465 | 2.896 | 2.157 | 4.203 | 16.413 | 3.166 | 2.508  | 3.945 | 4234.848  | 10.405 | 4.791 |
| Pgpool cluster(3) |       6027.996       |      13.271408      |     13.271679     | 6.569 | 6.529 | 14.449 | 637.22  | 10.497 | 2287.350 | 26.816 | 7.384 | 6.981 | 6.558 | 8.472 | 24.400 | 6.997 | 6.6027 | 8.339 | 2982.970 | 11.871 | 7.649 |

## . Statistical view

**Average Latency**

|                | Average Latency difference from optimal setup :  slave direct (in ms) | Average Latency difference from optimal setup (% over slave direct) |
|:--------------:|:---------------------------------------------------------------------:|:-------------------------------------------------------------------:|
|  Slave Direct  |                                + 0.00                                 |                                 0%                                  |
| Master Direct  |                               + 792.686                               |                                11.9%                                |
|     Pgpool     |                              + 1708.815                               |                                25.7%                                |
| Pgpool cluster(3) |                              -6.27.74899                              |                               -9.43%                                |

**Tps (Transaction per seconds)**

|                | tps with handshake difference from optimal setup (tps) | tps with handshake  difference from optimal setup (ratio over slave direct) | tps w/o handshake difference from optimal setup (tps) | tps w/o handshake  difference from optimal setup (ratio over slave direct) |
|:--------------:|:------------------------------------------------------:|:---------------------------------------------------------------------------:|:-----------------------------------------------------:|:--------------------------------------------------------------------------:|
|  Slave Direct  |                         + 0.00                         |                                     0%                                      |                        + 0.00                         |                                     0%                                     |
| Master Direct  |                       - 1.279175                       |                                   - 10.6%                                   |                      - 1.279145                       |                                  - 10.6%                                   |
|     Pgpool     |                       - 2.478072                       |                                   - 20.6%                                   |                       - 2.45631                       |                                  - 20.4%                                   |
| Pgpool cluster(3) |                       + 1.251716                       |                                   + 10.4%                                   |                      + 1.2518890                      |                                  +10.42%                                   |

Clustering pgpool seem to increase postgres performance drastically observable when it comes to more complex transactions such as those in Q15 and Q5. This is possibly due to the inane 'loadbalancing' provided by both service layers (pgpool-service) as well as pgpool load balancing mechanism. This comes at an lower performance for simple requests as the constant shifting and handshakes required makes simple queries unviable (sometimes with 300% average latencies than other methods)


## . In depth Analysis of Pgpool clustering performance
![](perf.png)

With our current setup (1 master and 2 replicas with pgpool instance each having affinity towards sharing nodes with postgres instances), 3 replicas seems to be the optimal setup to reduce both latency and increase tps. 
