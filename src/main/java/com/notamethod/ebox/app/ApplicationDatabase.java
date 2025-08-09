package com.notamethod.ebox.app;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;
import java.util.Scanner;


@Slf4j
public class ApplicationDatabase {

    EntityManagerFactory emf;// = Persistence.createEntityManagerFactory("ebox_pu");
    EntityManager em;// = emf.createEntityManager();

    public ApplicationDatabase(EntityManagerFactory emf) {
        this.emf = emf;
        em = emf.createEntityManager();
    }

    /**
     * Loads a application database
     *
     * @param name The name of the database
     * @return the database
     */
    public ApplicationList load(String name) {
        log.info("loading game.dat");
        String config = "";
        ApplicationList bl = new ApplicationList();
        Scanner s = null;
        try {
            s = new Scanner(new File(name));
        } catch (FileNotFoundException ex) {
            return new ApplicationList();
        }

        while (s.hasNext()) {
            config += s.nextLine() + "\n";
        }
        bl.readConfig(config);
        return bl;
    }


    /**
     * Writes the application database to a file
     *
     * @param fileName The filename that we write to
     */
    public void save(ApplicationList bl, String fileName) {
        FileWriter fstream = null;
        try {
            fstream = new FileWriter(fileName);
            BufferedWriter writer = new BufferedWriter(fstream);
            writer.write(bl.toConfigString());
            //Close the output stream
            writer.close();
        } catch (IOException ex) {
            log.error("error loading file", ex);
        } finally {
            try {
                fstream.close();
            } catch (IOException ex) {
                log.error("error loading file", ex);
            }
        }
    }



    /**
     * Writes the application database to a file
     *
     */
    public void save(GameEntity gameEntity) {
        em.getTransaction().begin();
        em.persist(gameEntity);
        em.getTransaction().commit();
    }

    public GameEntity findById(long id) {
        em.getTransaction().begin();
        Query q = em.createQuery ("SELECT p FROM GameEntity p where p.id=:id");
        //Query q = em.createQuery ("SELECT x FROM Magazine x WHERE x.title = :titleParam and x.price > :priceParam");
        q.setParameter ("id", id);
        GameEntity game = (GameEntity) q.getSingleResult();
        em.getTransaction().commit();
        return game;
    }

    public List<GameEntity> findByName(String name) {
        em.getTransaction().begin();
        Query q = em.createQuery ("SELECT p FROM GameEntity p where p.name=:name");
        //Query q = em.createQuery ("SELECT x FROM Magazine x WHERE x.title = :titleParam and x.price > :priceParam");
        q.setParameter ("name", name);
        List<GameEntity> results = q.getResultList ();

        em.getTransaction().commit();
        return results;
    }

    public List<GameEntity> loadAll() {
        em.getTransaction().begin();
        List<GameEntity> games = em.createQuery("SELECT p FROM GameEntity p", GameEntity.class).getResultList();
        em.getTransaction().commit();
        return games;
    }
//
//em.getTransaction().begin();
//    Person p = new Person();
//p.setName("Christophe");
//p.setEmail("christophe@example.com");
//em.persist(p);
//em.getTransaction().commit();
//
//    List<Person> people = em.createQuery("SELECT p FROM Person p", Person.class).getResultList();
}
