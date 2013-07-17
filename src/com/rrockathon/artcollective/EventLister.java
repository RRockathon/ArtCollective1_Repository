package com.rrockathon.artcollective;

import com.rrockathon.artcollective.Constants;
import com.rrockathon.artcollective.Lister;

public class EventLister extends Lister {
	@Override String getMyType() { return Constants.ARTS_EVENTS; }
}
