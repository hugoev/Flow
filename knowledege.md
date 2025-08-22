Vehicle Telemetry System
This project is a scalable, real-time IoT platform designed to process, store, and visualize vehicle sensor data. It uses a modern microservices architecture with a focus on high-throughput data ingestion and efficient data access.

Features
Real-time Data Processing: Ingest and process thousands of data points per second from multiple vehicle sensors.
Scalable Architecture: Built with Java microservices and Apache Kafka to handle massive data streams.
Containerized Deployment: Uses Docker for containerization and Kubernetes for orchestration, ensuring auto-scaling and fault tolerance.
Efficient Data Storage: Implements optimized data structures for fast querying and retrieval of telemetry insights.
Web Dashboard: A modern frontend built with Angular to visualize vehicle data in real time.
Motivations
I wanted to build a system that could handle the kind of data you'd get from a fleet of connected vehicles—where every vehicle is constantly sending off a ton of data. The core idea was to create a distributed system that wouldn't fall over under pressure. This project was a way for me to get hands-on experience with technologies like Kafka and Kubernetes, which are essential for building robust, scalable applications.

Growth and Learning
Distributed Systems: This project was my first real dive into distributed systems. I learned about the challenges of building a system where multiple components need to communicate and work together reliably. It taught me the importance of message queues and decoupling services.
DevOps and Orchestration: I had some experience with Docker, but using Kubernetes to manage a multi-service application was a new and challenging experience. I learned how to set up deployment files, manage services, and implement auto-scaling and fault tolerance—crucial skills for modern software development.
Performance Engineering: I had to think carefully about how to efficiently handle a high volume of data. This involved not just using Kafka, but also optimizing the Java microservices and implementing efficient data structures to ensure the system remained performant under load.
Technical Issues and Solutions
The main issue I faced was ensuring data integrity and preventing message loss during high-volume traffic spikes. To solve this, I leveraged Apache Kafka's built-in features for durable message storage and partitioning. This allowed me to distribute the load across multiple consumers and ensure that even if a service went down, the messages would be waiting for it when it came back online. Another challenge was managing the complexity of multiple interacting microservices, which I solved by using Kubernetes to handle service discovery and health checks automatically.
Technologies Used
Backend: Java, Spring Boot, Apache Kafka
Containerization & Orchestration: Docker, Kubernetes
Database: Apache Cassandra
Frontend: Angular, TypeScript
APIs: REST API
Tools: Git, Maven
