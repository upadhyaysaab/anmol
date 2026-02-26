✔ In EKS, your Java microservice runs as a JAR
with embedded Tomcat inside it.

✔ Tomcat is INTERNAL (inside the JAR → inside the container → inside the Pod).
So we don’t talk about it at Kubernetes level.

✔ ALB / NGINX are EXTERNAL
They work at networking/ingress layer → that’s why all architecture talks mention ALB/NGINX, 
not Tomcat

✔ WAR apps are legacy
JAR with embedded server is the microservice standard.

-------

Docker file for java (.jar)

FROM eclipse-temurin:11-jre-jammy
RUN apt-get update && apt-get upgrade -y
WORKDIR /app
COPY target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

------

OOM ERROR 

Kubernetes mein OOM tab hota hai jab container apne memory limit ko cross kar deta hai.
Isko fix karne ke liye hum pod ki memory limits badhate hain, JVM ko container‑aware settings (jaise MaxRAMPercentage) ke saath tune karte hain, heap usage analyze karte hain, aur traffic spikes handle karne ke liye HPA enable karte hain.
OOMKilled ka exact reason check karne ke liye hum kubectl describe pod use karte hain.”


Troubleshhoting

✅ 1. Increase Pod Memory Limit
Most common & correct approach.
Example (Deployment):
YAMLresources:  requests:    memory: "512Mi"  limits:    memory: "1Gi"

If your Java app actually needs 1GB and your limit is 512Mi → it will get OOMKilled.
✔ Give enough memory
✔ But not too much (avoid node pressure)

✅ 2. Set JVM heap correctly (VERY important)
Problem:

JVM doesnt know about Kubernetes memory limits.
If pod limit is 1Gi and JVM takes 1Gi heap,
then JVM + metaspace + threads ⇒ exceeds limit ⇒ OOMKilled.
Fix:
Use container‑aware JVM flags:
-XX:+UseContainerSupport
-XX:MaxRAMPercentage=75.0

So if pod has 1GB memory, JVM uses only 750MB.
Add in Dockerfile:
DockerfileENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]Show more lines
Perfect for preventing OOMKilled.

✅ 3. Check for REAL Java memory leaks
If your heaps keep growing → leak.
Do:
kubectl logs <pod>

Look for:

OutOfMemoryError: Java heap space
OutOfMemoryError: Metaspace

If JVM causes OOM (not K8s), fix by:

Analyzing heap dump
Leaky collections
Too many threads
Bad caching
Hibernate lazy loads


✅ 4. Enable requests = limits for memory (optional)
Useful for predictable memory footprint:
YAMLresources:  requests:    memory: "1Gi"  limits:    memory: "1Gi"
This avoids sudden spikes.

✅ 5. Use HPA to autoscale pods
If traffic spikes → memory spikes.
Enable autoscaling:
kubectl autoscale deployment myapp --cpu-percent=70 --min=2 --max=10

More pods = less load per pod.

✅ 6. Use liveness/readiness probes
If pod is unhealthy, reload it gracefully instead of letting it OOM.
Example:
YAMLlivenessProbe:  httpGet:    path: /actuator/health/liveness    port: 8080  initialDelaySeconds: 30Show more lines

✅ 7. Look at Pod OOM events (most important debugging tool)
Run:
kubectl describe pod <pod>

Check at bottom:
Last State: OOMKilled
Reason:     Memory cgroups limit exceeded

This confirms Kubernetes killed it — not your app.

------- HOW TO FIX HEAP DUMPS:-- (Memory)

METHOD 1 — Using jcmd (BEST & CLEANEST)
Step 1: Exec into the pod
kubectl exec -it <pod-name> -- sh

Step 2: Find the Java process ID
Usually PID = 1 (Spring Boot), but check:
ps -ef | grep java

Step 3: Run heap dump command
jcmd 1 GC.heap_dump /tmp/heapdump.hprofShow 

Step 4: Copy the dump to local machine
First exit pod, then:
kubectl cp <pod-name>:/tmp/heapdump.hprof heapdump.hprofShow more lines
✨ You can now open it using Eclipse MAT or VisualVM.


-------------HOW TO FIX I/O  bottleneck issue----

I/O bottleneck is when MySQL storage cannot keep up with read/write pressure.
This causes high latency, slow queries, and queue build‑up.
Causes:

Missing indexes → full table scan
Heavy writes
Small buffer pool
Temp table disk spills
Slow storage (gp2)

How to detect:

CloudWatch ReadLatency/WriteLatency high
DiskQueueDepth high
High IOPS usage
Performance Insights showing IO waits
Slow queries hitting disk

Fixes:

Add indexes
Increase RAM
Faster storage (gp3/io2)
Optimize queries
Use replicas

🛠 How to Fix IO Bottlenecks
🟩 Fix 1: Add Proper Indexes (Most Important)
Avoid full table scans.
🟩 Fix 2: Increase Buffer Pool (instance upgrade)
More RAM = less disk usage.
🟩 Fix 3: Reduce Writes

Batch commits
Avoid huge transactions
Remove unnecessary secondary indexes

🟩 Fix 4: Upgrade Storage

gp2 → gp3 (higher baseline)
gp3 → io2 (dedicated IOPS)

🟩 Fix 5: Optimize Queries
Avoid:

SELECT *
Large joins
ORDER BY without index
GROUP BY heavy

-------------------------------------------

TL;DR

Yes, M6i is good for EKS worker nodes (general-purpose microservices).
Use gp3 for most pods’ EBS needs; switch to io2 for stateful/high‑IOPS workloads.

-------CPU saturation------

1️⃣ Kill unused processes / fix bad threads
2️⃣ Fix CPU requests & limits for pods
3️⃣ Enable HPA (horizontal pod autoscaling)
4️⃣ Use cluster autoscaler / Karpetner (add more nodes)
5️⃣ Use Load Balancer to distribute traffic
6️⃣ Fix database bottlenecks (indexes, slow queries)
7️⃣ Tune application (GC, loops, code inefficiency)
8️⃣ Remove noisy sidecars / optimize logging
9️⃣ ONLY THEN → Scale instance size (m6i → m6i.large → m6i.xlarge)
