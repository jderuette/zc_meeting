package org.zeroclick.configuration.client.slot;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.TreeNodeData;
import org.zeroclick.common.ui.form.IPageForm;
import org.zeroclick.configuration.client.slot.SlotForm.MainBox.CloseButton;
import org.zeroclick.configuration.client.slot.SlotForm.MainBox.OkButton;
import org.zeroclick.configuration.client.slot.SlotForm.MainBox.SlotsBox;
import org.zeroclick.configuration.client.slot.SlotForm.MainBox.SlotsBox.SlotSelectorGroupBox.SlotSelectorField;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.SlotFormData;
import org.zeroclick.meeting.shared.security.AccessControlService;

@FormData(value = SlotFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class SlotForm extends AbstractForm implements IPageForm {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.slot.config");
	}

	@Override
	public void startPageForm() {
		this.startInternal(new PageFormHandler());
	}

	@Override
	public AbstractCloseButton getCloseButton() {
		return this.getFieldByClass(CloseButton.class);
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public SlotsBox getSlotsBox() {
		return this.getFieldByClass(SlotsBox.class);
	}

	public SlotSelectorField getSlotSelectorField() {
		return this.getFieldByClass(SlotSelectorField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	protected void loadDayDurationForm(final Long nodeId) {
		final List<IForm> forms = SlotForm.this.getDesktop().getForms(SlotForm.this.getDesktop().getOutline());

		String slotName;
		final ITreeNode curentNode = this.getSlotSelectorField().getTree().getSelectedNode();
		final Boolean isRootNode = null == curentNode.getParentNode();

		if (isRootNode) {
			slotName = curentNode.getCell().getText();
		} else {
			slotName = curentNode.getParentNode().getCell().getText() + " - " + curentNode.getCell().getText();
		}

		for (final IForm form : forms) {
			if (null != slotName && slotName.equals(form.getSubTitle())) {
				form.activate();
				// early break;
				return;
			}
		}

		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final DayDurationForm dayDurationForm = new DayDurationForm();

		dayDurationForm.setUserId(acs.getZeroClickUserIdOfCurrentSubject());
		dayDurationForm.setSubTitle(slotName);
		dayDurationForm.getMainBox().setGridColumnCountHint(1);
		dayDurationForm.setDayDurationId(nodeId);
		dayDurationForm.setSlotId((Long) curentNode.getParentNode().getPrimaryKey());
		dayDurationForm.setDisplayParent(SlotForm.this.getDesktop().getOutline());
		dayDurationForm.setDisplayHint(DISPLAY_HINT_VIEW);
		dayDurationForm.setDisplayViewId(VIEW_ID_E);

		dayDurationForm.startModify();
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		public class EditMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.meeting.dayDuration.edit");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TreeMenuType.SingleSelection, TreeMenuType.MultiSelection);
			}

			@Override
			protected void execAction() {
				final Set<ITreeNode> selectedNodes = SlotForm.this.getSlotSelectorField().getTree().getSelectedNodes();
				if (null != selectedNodes && !selectedNodes.isEmpty()) {
					for (final ITreeNode node : selectedNodes) {
						if (null != node && node.isLeaf()) {
							SlotForm.this.loadDayDurationForm((Long) node.getPrimaryKey());
						}
					}
				}
			}
		}

		@Order(0)
		public class SlotsBox extends AbstractGroupBox {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.slot.config");
			}

			@Order(1000)
			public class SlotSelectorGroupBox extends AbstractGroupBox {

				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.slot.config");
				}

				@Override
				protected boolean getConfiguredLabelVisible() {
					return Boolean.FALSE;
				}

				@Order(1000)
				public class SlotSelectorField extends AbstractTreeField {
					public class Tree extends AbstractTree {

						@Order(1000)
						public class EditMenu extends AbstractMenu {
							@Override
							protected String getConfiguredText() {
								return TEXTS.get("zc.meeting.dayDuration.edit");
							}

							@Override
							protected Set<? extends IMenuType> getConfiguredMenuTypes() {
								return CollectionUtility.hashSet(TreeMenuType.SingleSelection,
										TreeMenuType.MultiSelection);
							}

							@Override
							protected void execAction() {
								final Set<ITreeNode> selectedNodes = SlotForm.this.getSlotSelectorField().getTree()
										.getSelectedNodes();
								if (null != selectedNodes && !selectedNodes.isEmpty()) {
									for (final ITreeNode node : selectedNodes) {
										if (null != node && node.isLeaf()) {
											SlotForm.this.loadDayDurationForm((Long) node.getPrimaryKey());
										}
									}
								}
							}
						}
					}

					@Order(1000)
					public class EditKeyStroke extends AbstractKeyStroke {
						@Override
						protected String getConfiguredKeyStroke() {
							return IKeyStroke.ENTER;
						}

						@Override
						protected String getConfiguredText() {
							return TEXTS.get("zc.meeting.dayDuration.edit");
						}

						@Override
						protected void execAction() {
							final Set<ITreeNode> selectedNodes = SlotForm.this.getSlotSelectorField().getTree()
									.getSelectedNodes();
							if (null != selectedNodes && !selectedNodes.isEmpty()) {
								for (final ITreeNode node : selectedNodes) {
									if (null != node && node.isLeaf()) {
										SlotForm.this.loadDayDurationForm((Long) node.getPrimaryKey());
									}
								}
							}
						}
					}

					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("zc.meeting.slot");
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return Boolean.FALSE;
					}

					@Override
					protected boolean getConfiguredAutoExpandAll() {
						return Boolean.TRUE;
					}

					@Override
					protected int getConfiguredGridH() {
						return 10;
					}

					@Override
					protected String getConfiguredTooltipText() {
						return TEXTS.get("zc.meeting.slot.tree.tooltip");
					}

					@Override
					protected void execInitField() {

						super.execInitField();

						this.addNodes(this.getTree(), this.getTree().getRootNode());
						this.getTree().addTreeListener(this.listener);

						this.getTree().setMultiSelect(Boolean.TRUE);
					}

					private void addNodes(final ITree tree, final ITreeNode parent) {
						final ISlotService slotService = BEANS.get(ISlotService.class);

						final Object[][] slots = slotService.getSlots();

						if (null != slots) {
							for (int r = 0; r < slots.length; r++) {
								if (null != slots[r]) {
									final Object[] currentSlot = slots[r];
									final ITreeNode node = this.newNode((Long) currentSlot[0], (String) currentSlot[1]);
									tree.addChildNode(parent, node);

									final Integer nbDayDurationConfig = this.addDayDurationNode((Long) currentSlot[0],
											node);

									if (nbDayDurationConfig > 1) {
										node.setExpanded(Boolean.TRUE);
									}
								}
							}
						}
					}

					private Integer addDayDurationNode(final Long slotId, final ITreeNode slotNode) {
						Integer nbDayDuration = 0;
						final ISlotService slotService = BEANS.get(ISlotService.class);

						final Object[][] dayDuration = slotService.getDayDurationsLight(slotId);

						if (null != dayDuration) {
							nbDayDuration = dayDuration.length;
							for (int r = 0; r < dayDuration.length; r++) {
								if (null != dayDuration[r]) {
									final ITreeNode node = this.newNode((Long) dayDuration[r][0],
											(String) dayDuration[r][1]);
									node.setLeaf(Boolean.TRUE);
									slotNode.getTree().addChildNode(slotNode, node);
								}
							}
						}
						return nbDayDuration;
					}

					protected TreeNodeData newTreeNodeData(final String key) {
						final TreeNodeData node = new TreeNodeData();
						final String text = TEXTS.get(key);
						node.setTexts(CollectionUtility.arrayList(text));
						return node;
					}

					protected ITreeNode newNode(final Long nodePrimaryKey, final String key) {
						final ITreeNode node = new AbstractTreeNode() {
						};
						final String text = TEXTS.get(key);
						node.getCellForUpdate().setText(text);
						node.setPrimaryKey(nodePrimaryKey);
						node.setExpanded(Boolean.TRUE);
						return node;
					}

					public TreeListener listener = new TreeListener() {

						@Override
						public void treeChangedBatch(final List<? extends TreeEvent> batch) {
							// TODO Auto-generated method stub
						}

						@Override
						public void treeChanged(final TreeEvent e) {
							if (TreeEvent.TYPE_NODE_ACTION == e.getType()) {
								final ITreeNode clickedNode = e.getNode();
								// only leaf node can be edited
								if (clickedNode.isLeaf()) {
									SlotForm.this.loadDayDurationForm((Long) clickedNode.getPrimaryKey());
								}
							}
						}
					};
				}
			}
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(50)
		public class CloseButton extends AbstractCloseButton {
		}
	}

	public class PageFormHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
		}
	}
}
