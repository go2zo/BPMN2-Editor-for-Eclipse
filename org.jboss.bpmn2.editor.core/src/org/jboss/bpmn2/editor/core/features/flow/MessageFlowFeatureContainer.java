package org.jboss.bpmn2.editor.core.features.flow;

import java.io.IOException;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.InteractionNode;
import org.eclipse.bpmn2.MessageFlow;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeService;
import org.eclipse.graphiti.util.IColorConstant;
import org.jboss.bpmn2.editor.core.Activator;
import org.jboss.bpmn2.editor.core.ImageProvider;
import org.jboss.bpmn2.editor.core.ModelHandler;
import org.jboss.bpmn2.editor.core.features.BusinessObjectUtil;
import org.jboss.bpmn2.editor.core.features.ConnectionFeatureContainer;
import org.jboss.bpmn2.editor.core.features.FeatureSupport;
import org.jboss.bpmn2.editor.core.features.StyleUtil;

public class MessageFlowFeatureContainer extends ConnectionFeatureContainer {

	@Override
	public boolean canApplyTo(BaseElement element) {
		return element instanceof MessageFlow;
	}

	@Override
	public IAddFeature getAddFeature(IFeatureProvider fp) {
		return new AbstractAddFlowFeature(fp) {

			@Override
			void decorateConnectionLine(Polyline connectionLine) {
				connectionLine.setLineStyle(LineStyle.DASH);
			}

			@Override
			void createConnectionDecorators(Connection connection) {
				IPeService peService = Graphiti.getPeService();
				IGaService gaService = Graphiti.getGaService();

				ConnectionDecorator endDecorator = peService.createConnectionDecorator(connection, false, 1.0, true);
				ConnectionDecorator startDecorator = peService.createConnectionDecorator(connection, false, 0, true);

				int w = 5;
				int l = 15;

				Polyline polyline = gaService.createPolygon(endDecorator, new int[] { -l, w, 0, 0, -l, -w, -l, w });
				polyline.setForeground(manageColor(StyleUtil.CLASS_FOREGROUND));
				polyline.setBackground(manageColor(IColorConstant.WHITE));
				polyline.setFilled(true);
				polyline.setLineWidth(1);

				Ellipse ellipse = gaService.createEllipse(startDecorator);
				ellipse.setForeground(manageColor(StyleUtil.CLASS_FOREGROUND));
				ellipse.setBackground(manageColor(IColorConstant.WHITE));
				ellipse.setFilled(true);
				ellipse.setLineWidth(1);
				gaService.setSize(ellipse, 10, 10);
			}

			@Override
			Class<? extends BaseElement> getBoClass() {
				return MessageFlow.class;
			}
		};
	}

	@Override
	public ICreateConnectionFeature getCreateConnectionFeature(IFeatureProvider fp) {
		return new CreateMessageFlowFeature(fp);
	}

	public static class CreateMessageFlowFeature extends AbstractCreateFlowFeature<InteractionNode> {

		public CreateMessageFlowFeature(IFeatureProvider fp) {
			super(fp, "Message Flow", "Represents message between two participants");
		}

		@Override
		public boolean canCreate(ICreateConnectionContext context) {
			InteractionNode source = getFlowNode(context.getSourceAnchor());
			InteractionNode target = getFlowNode(context.getTargetAnchor());
			return super.canCreate(context) && isDifferentParticipants(source, target);
		}

		@Override
		BaseElement createFlow(ModelHandler mh, InteractionNode source, InteractionNode target) {
			MessageFlow flow = mh.createMessageFlow(source, target);
			flow.setName("Message Flow");
			return flow;
		}

		@Override
		InteractionNode getFlowNode(Anchor anchor) {
			if (anchor != null) {
				return (InteractionNode) BusinessObjectUtil.getFirstElementOfType(anchor.getParent(),
				        InteractionNode.class);
			}
			return null;
		}

		@Override
		String getStencilImageId() {
			return ImageProvider.IMG_16_MESSAGE_FLOW;
		}

		private boolean isDifferentParticipants(InteractionNode source, InteractionNode target) {
			if (source == null || target == null) {
				return true;
			}
			boolean different = false;
			try {
				ModelHandler handler = FeatureSupport.getModelHanderInstance(getDiagram());
				different = !handler.getParticipant(source).equals(handler.getParticipant(target));
			} catch (IOException e) {
				Activator.logError(e);
			}
			return different;
		}
	}
}