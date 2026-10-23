package ca.mcgill.ecse428.bugbusters.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
@IdClass(RoleId.class)
public class Role {

    @Id
    @Column(name = "movie_id")
    private String movieId;

    @Id
    @Column(name = "person_id")
    private String personId;

    @Column(name = "role")
    private String role;

    public Role() {
    }

    public Role(String movieId, String personId, String role) {
        this.movieId = movieId;
        this.personId = personId;
        this.role = role;
    }

    public String getMovifeId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}