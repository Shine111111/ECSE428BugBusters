package ca.mcgill.ecse428.bugbusters.model;

import java.io.Serializable;
import java.util.Objects;

public class RoleId implements Serializable {
    private String movieId;
    private String personId;

    public RoleId() {
    }

    public RoleId(String movieId, String personId) {
        this.movieId = movieId;
        this.personId = personId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleId)) return false;
        RoleId roleId = (RoleId) o;
        return Objects.equals(movieId, roleId.movieId) &&
               Objects.equals(personId, roleId.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, personId);
    }
}