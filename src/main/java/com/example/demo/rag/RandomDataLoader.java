package com.example.demo.rag;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.qdrant.client.QdrantClient;
import jakarta.annotation.PostConstruct;

@Component
public class RandomDataLoader {

    private final VectorStore vectorStore;
    private final String collectionName;

    RandomDataLoader(VectorStore vectorStore,
                     @Value("${spring.ai.vectorstore.qdrant.collection-name}") String collectionName){
        this.vectorStore = vectorStore;
        this.collectionName = collectionName;
    }

    @PostConstruct
    public void loadSentencesIntoVectors(){
        // Skip if Qdrant already has data, so restarts don't re-embed or duplicate the sentences
        long existing = countPoints();
        if (existing > 0) {
            System.out.println("Vector store already has " + existing + " points, skipping load");
            return;
        }

        List<String> sentences = List.of(
            // Space
            "The Sun is a medium-sized star located at the center of our solar system.",
            "Mars is often called the Red Planet because of the iron oxide on its surface.",
            "Jupiter is the largest planet in the solar system and has a giant storm called the Great Red Spot.",
            "Astronauts aboard the International Space Station orbit Earth about every 90 minutes.",
            "A black hole is a region of space where gravity is so strong that not even light can escape.",

            // Programming
            "Java is an object-oriented programming language that runs on the Java Virtual Machine.",
            "Spring Boot makes it easy to create stand-alone, production-grade Spring applications.",
            "Python is popular for data science because of libraries like NumPy and pandas.",
            "Git is a distributed version control system used to track changes in source code.",
            "A REST API uses HTTP methods such as GET, POST, PUT, and DELETE to manage resources.",

            // Artificial Intelligence
            "A large language model is trained on huge amounts of text to predict the next word.",
            "Embeddings convert text into numerical vectors that capture semantic meaning.",
            "A vector database stores embeddings and supports fast similarity search.",
            "Retrieval-Augmented Generation combines document search with a language model to answer questions.",
            "Cosine similarity measures how close two vectors are by the angle between them.",

            // Cooking
            "Pasta should be cooked in salted boiling water until it is al dente.",
            "Biryani is a fragrant rice dish cooked with spices, meat or vegetables, and saffron.",
            "Baking bread requires flour, water, yeast, and salt, along with time for the dough to rise.",
            "Dosa is a thin, crispy South Indian pancake made from fermented rice and lentil batter.",
            "Caramelizing onions slowly over low heat brings out their natural sweetness.",

            // Health and Fitness
            "Drinking enough water every day helps regulate body temperature and supports digestion.",
            "Regular aerobic exercise like running or swimming strengthens the heart and lungs.",
            "Adults generally need seven to nine hours of sleep each night to stay healthy.",
            "Yoga improves flexibility, balance, and mental focus through postures and breathing.",
            "A balanced diet includes proteins, carbohydrates, healthy fats, vitamins, and minerals.",

            // Animals
            "Elephants are the largest land animals and are known for their strong memory.",
            "Dolphins are highly intelligent marine mammals that communicate using clicks and whistles.",
            "The cheetah is the fastest land animal and can reach speeds of about 110 kilometers per hour.",
            "Honeybees pollinate flowers and produce honey from the nectar they collect.",
            "Penguins are flightless birds that use their wings to swim in cold ocean waters.",

            // Sports
            "Cricket is a bat-and-ball game played between two teams of eleven players.",
            "Football, also called soccer, is the most popular sport in the world.",
            "The Olympic Games are held every four years and bring together athletes from many nations.",
            "Tennis players compete in Grand Slam tournaments like Wimbledon and the US Open.",
            "A marathon is a long-distance running race covering 42.195 kilometers.",

            // Finance
            "Compound interest means you earn interest on both your savings and the interest already earned.",
            "Diversifying investments across different assets helps reduce financial risk.",
            "Inflation is the rate at which the general price of goods and services rises over time.",
            "An emergency fund should ideally cover three to six months of living expenses.",
            "Stocks represent ownership in a company, while bonds are loans made to a company or government.",

            // Travel and Geography
            "The Eiffel Tower in Paris was completed in 1889 and is one of the most visited monuments in the world.",
            "Mount Everest, located in the Himalayas, is the highest mountain above sea level.",
            "The Amazon rainforest produces a significant portion of the world's oxygen and is home to countless species.",
            "Kerala in India is famous for its backwaters, houseboats, and coconut trees.",
            "The Sahara is the largest hot desert in the world, covering much of North Africa.",

            // History
            "The Great Wall of China was built over centuries to protect against invasions.",
            "The Taj Mahal was built by Mughal emperor Shah Jahan in memory of his wife Mumtaz Mahal.",
            "World War II lasted from 1939 to 1945 and involved most of the world's nations.",
            "The Industrial Revolution began in Britain in the 18th century and transformed manufacturing.",
            "India gained independence from British rule on 15 August 1947.",

            // Music
            "The piano has 88 keys and can play both melody and harmony at the same time.",
            "Carnatic music is a classical music tradition from South India based on ragas and talas.",
            "The guitar is a stringed instrument usually played by strumming or plucking the strings.",
            "Beethoven composed some of his greatest works even after he lost his hearing.",
            "Rhythm is the pattern of beats and timing that gives music its sense of movement."
        );

        // Same sentence -> same ID, so even if this runs again Qdrant overwrites instead of duplicating
        List<Document> documents = sentences.stream()
                .map(sentence -> new Document(idFor(sentence), sentence, Map.of()))
                .toList();

        vectorStore.add(documents);
    }

    private long countPoints() {
        try {
            QdrantClient client = vectorStore.<QdrantClient>getNativeClient().orElseThrow();
            return client.countAsync(collectionName).get();
        } catch (Exception e) {
            return 0;
        }
    }

    private static String idFor(String sentence) {
        return UUID.nameUUIDFromBytes(sentence.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
