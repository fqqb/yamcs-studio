/*
 * generated by Xtext
 */
package org.csstudio.yamcs.ycl.dsl.ui.contentassist.antlr;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.RecognitionException;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.ui.editor.contentassist.antlr.AbstractContentAssistParser;
import org.eclipse.xtext.ui.editor.contentassist.antlr.FollowElement;
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.AbstractInternalContentAssistParser;

import com.google.inject.Inject;

import org.csstudio.yamcs.ycl.dsl.services.YCLGrammarAccess;

public class YCLParser extends AbstractContentAssistParser {
	
	@Inject
	private YCLGrammarAccess grammarAccess;
	
	private Map<AbstractElement, String> nameMappings;
	
	@Override
	protected org.csstudio.yamcs.ycl.dsl.ui.contentassist.antlr.internal.InternalYCLParser createParser() {
		org.csstudio.yamcs.ycl.dsl.ui.contentassist.antlr.internal.InternalYCLParser result = new org.csstudio.yamcs.ycl.dsl.ui.contentassist.antlr.internal.InternalYCLParser(null);
		result.setGrammarAccess(grammarAccess);
		return result;
	}
	
	@Override
	protected String getRuleName(AbstractElement element) {
		if (nameMappings == null) {
			nameMappings = new HashMap<AbstractElement, String>() {
				private static final long serialVersionUID = 1L;
				{
					put(grammarAccess.getREALAccess().getAlternatives_2(), "rule__REAL__Alternatives_2");
					put(grammarAccess.getArgumentAssignmentValueAccess().getAlternatives(), "rule__ArgumentAssignmentValue__Alternatives");
					put(grammarAccess.getREALAccess().getGroup(), "rule__REAL__Group__0");
					put(grammarAccess.getCommandAccess().getGroup(), "rule__Command__Group__0");
					put(grammarAccess.getCommandAccess().getGroup_1(), "rule__Command__Group_1__0");
					put(grammarAccess.getArgumentAssignmentAccess().getGroup(), "rule__ArgumentAssignment__Group__0");
					put(grammarAccess.getModelAccess().getCommandsAssignment(), "rule__Model__CommandsAssignment");
					put(grammarAccess.getCommandAccess().getNameAssignment_0(), "rule__Command__NameAssignment_0");
					put(grammarAccess.getCommandAccess().getAssignmentsAssignment_1_1(), "rule__Command__AssignmentsAssignment_1_1");
					put(grammarAccess.getCommandIdAccess().getIdAssignment(), "rule__CommandId__IdAssignment");
					put(grammarAccess.getArgumentAssignmentAccess().getNameAssignment_0(), "rule__ArgumentAssignment__NameAssignment_0");
					put(grammarAccess.getArgumentAssignmentAccess().getValueAssignment_2(), "rule__ArgumentAssignment__ValueAssignment_2");
				}
			};
		}
		return nameMappings.get(element);
	}
	
	@Override
	protected Collection<FollowElement> getFollowElements(AbstractInternalContentAssistParser parser) {
		try {
			org.csstudio.yamcs.ycl.dsl.ui.contentassist.antlr.internal.InternalYCLParser typedParser = (org.csstudio.yamcs.ycl.dsl.ui.contentassist.antlr.internal.InternalYCLParser) parser;
			typedParser.entryRuleModel();
			return typedParser.getFollowElements();
		} catch(RecognitionException ex) {
			throw new RuntimeException(ex);
		}		
	}
	
	@Override
	protected String[] getInitialHiddenTokens() {
		return new String[] { "RULE_WS", "RULE_SL_COMMENT" };
	}
	
	public YCLGrammarAccess getGrammarAccess() {
		return this.grammarAccess;
	}
	
	public void setGrammarAccess(YCLGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}
}
