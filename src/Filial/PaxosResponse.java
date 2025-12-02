package Filial;

public class PaxosResponse {
    public enum Status { OK, ACCEPTED, REJECT }
    public final Status status;
    public final String acceptedProposalId;
    public final String acceptedValue;

    public PaxosResponse(Status status, String acceptedProposalId, String acceptedValue) {
        this.status = status;
        this.acceptedProposalId = acceptedProposalId;
        this.acceptedValue = acceptedValue;
    }
}
