package SimulableModel.Link;

import DataTypes.PreconditionsException;
import SimulableModel.LinkType.LinkTypeSimulableReactionComprises;
import SimulableModel.SimulableModel;
import SimulableModel.SimulableReaction;

public class LinkSimulableReactionComprises {
    private LinkSimulableReactionComprises(){}

    public static void insertLink(SimulableModel model, SimulableReaction reaction) throws PreconditionsException {
        // Creo un pass: solo i metodi di questa classe possono farlo!
        LinkSimulableReactionComprises pass = new LinkSimulableReactionComprises();
        // Creo il link
        LinkTypeSimulableReactionComprises link = new LinkTypeSimulableReactionComprises(model, reaction);
        // Inserisco il link nei due oggetti esibendo il pass
        model.insertLinkSimulableReactionComprises(pass, link);
        reaction.insertLinkSimulableReactionComprises(pass, link);
    }

    public static void removeLink(LinkTypeSimulableReactionComprises link)
            throws PreconditionsException {
        LinkSimulableReactionComprises pass = new LinkSimulableReactionComprises();
        link.getSimulableModel().removeLinkSimulableReactionComprises(pass, link);
        link.getSimulableReaction().removeLinkSimulableReactionComprises(pass);
    }
}
