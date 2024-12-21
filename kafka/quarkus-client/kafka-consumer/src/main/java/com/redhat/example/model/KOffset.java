package com.redhat.example.model;

import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
//@IdClass(KOffsetPK.class)
public class KOffset extends PanacheEntityBase {

    @Id
    public String topic;

    @Id
    public int partition;

    public Long offsetCount;

    public static Optional<KOffset> findByIdOptional(String topic, int partition) {
        PanacheQuery<PanacheEntityBase> result = find("topic", topic);
        
        if (result.count()>0)
            return Optional.of(result.firstResult());
        else
            return Optional.empty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        result = prime * result + partition;
        result = prime * result + ((offsetCount == null) ? 0 : offsetCount.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KOffset other = (KOffset) obj;
        if (topic == null) {
            if (other.topic != null)
                return false;
        } else if (!topic.equals(other.topic))
            return false;
        if (partition != other.partition)
            return false;
        if (offsetCount == null) {
            if (other.offsetCount != null)
                return false;
        } else if (!offsetCount.equals(other.offsetCount))
            return false;
        return true;
    }
}
