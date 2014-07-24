package eu.mico.platform.broker.model;

/**
 * A symbolic representation of the type of input required and output produced by a service. Currently just uses
 * a string identifier to represent the type.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class TypeDescriptor {

    private String symbol;

    public TypeDescriptor(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeDescriptor that = (TypeDescriptor) o;

        if (!symbol.equals(that.symbol)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return symbol.hashCode();
    }
}
