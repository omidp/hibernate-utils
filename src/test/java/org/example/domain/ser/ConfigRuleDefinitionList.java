package org.example.domain.ser;

import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@NoArgsConstructor
public class ConfigRuleDefinitionList extends LinkedHashSet<ConfigRule> {

	public ConfigRuleDefinitionList(Set<ConfigRule> ruleDefinitions) {
		super.addAll(ruleDefinitions);
	}
}