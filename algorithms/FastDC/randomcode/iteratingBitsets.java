public DenialConstraintSet discoverDenialConstraints(IEvidenceSet evidenceSet) {
		Collection<IPredicateSet> MC = searchMinimalCovers(evidenceSet);
		
		for(IPredicateSetBinary evi: evidenceSet) {
			System.out.println(evi.getBitset() +" " +evidenceSet.getCount(evi) );
			System.out.println(evi.convert().getPredicates());
			System.out.println();
			
			
			
			for (IPredicateSet mc : MC) {
				
				
				System.out.println(mc.getPredicates());
				System.out.println(mc.getIPredicateSetBinary().getBitset());
				System.out.println(mc.getIPredicateSetBinary().convert());
				System.out.println(mc.getIPredicateSetBinary().getBitset().getAnd(evi.getBitset()));
				System.out.println(PredicateSetFactory.create( mc.getIPredicateSetBinary().getBitset().getAnd(evi.getBitset()) ).convert());	
				
				System.out.println();

			}
			
			
			
			
			
			System.out.println("**********************");
		}
		
		System.out.println(" ");
		
		

		log.info("Building DCs ...");
		DenialConstraintSet dcs = new DenialConstraintSet();
		for (IPredicateSet mc : MC) {
			
			DenialConstraint dc = new DenialConstraint(mc);
			dcs.add(dc);

		}

		dcs.minimalize();

		return dcs;
	}