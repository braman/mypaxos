package node;

public class Priest implements Comparable<Priest> {
	
	public Integer priestNo;
	public String  ip;
	
	public long lastSignal; //last signal time
	
	public Priest(String ip, Integer priestNo) {
    	this.priestNo = priestNo;
    	this.ip = ip;
    }
	
	@Override
	public String toString() {
		return String.format("ip:%s, priest:%s", ip, priestNo);
	}

	@Override
	public int compareTo(Priest o) {
		return priestNo.compareTo(o.priestNo);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((priestNo == null) ? 0 : priestNo.hashCode());
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
		Priest other = (Priest) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (priestNo == null) {
			if (other.priestNo != null)
				return false;
		} else if (!priestNo.equals(other.priestNo))
			return false;
		return true;
	}
	
	
	
}
