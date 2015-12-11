package me.scai.handwriting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CHandWritingTokenImageData {
	public String tokenName = null;
	public int nw = 0;
	public int nh = 0;	
	public int nStrokes = 0; /* Number of strokes */
	public float w;
	public float h;

	public Double[] imData = null;

	public CHandWritingTokenImageData() {
	};

	public CHandWritingTokenImageData(String t_tokenName) {
		tokenName = t_tokenName;
	}

	public CHandWritingTokenImageData(String t_tokenName, int t_nw, int t_nh) {
		tokenName = t_tokenName;
		nw = t_nw;
		nh = t_nh;
	}

	public static CHandWritingTokenImageData readImFile(File f, 
														boolean bIncludeTokenSize, 
														boolean bIncludeTokenWHRatio,
														boolean bIncludeTokenNumStrokes)
			throws IOException {
		final String TOKEN_NAME_LABEL = "Token name: ";
		final String N_W_LABEL = "n_w = ";
		final String N_H_LABEL = "n_h = ";
		final String NS_LABEL = "ns = ";
		final String W_LABEL = "w = ";
		final String H_LABEL = "h = ";

		CHandWritingTokenImageData dat = new CHandWritingTokenImageData();

		FileInputStream fin = null;
		BufferedReader in = null;
		try {
			fin = new FileInputStream(f);
			in = new BufferedReader(new InputStreamReader(fin));
		} catch (IOException e) {
			throw new IOException();
		}

		String line = null;
		int line_n = 0;

		/* Number of double values before the start of the image data */
		int nExtra = 0;
		if ( bIncludeTokenSize )
			nExtra += 2;
		if ( bIncludeTokenWHRatio )
			nExtra += 1;
		if ( bIncludeTokenNumStrokes )
			nExtra += 1;
		
		/* Current 3: w, h, whr and ns */
		//int dcnt = 4;
		int dcnt = 0;		
		
		double w = -1.0; /* Token width */
		double h = -1.0; /* Token height */

		while ((line = in.readLine()) != null) {
			if (line_n == 0) {
				if (line.startsWith(TOKEN_NAME_LABEL)) {
					dat.tokenName = line.replaceFirst(TOKEN_NAME_LABEL, "");
				} else {
					in.close();
					throw new IOException();
				}
			} else if (line_n == 1) {
				if (line.startsWith(N_W_LABEL)) {
					dat.nw = Integer.parseInt(line.replaceFirst(N_W_LABEL, ""));
				} else {
					in.close();
					throw new IOException();
				}
			} else if (line_n == 2) {
				if (line.startsWith(N_H_LABEL)) {
					dat.nh = Integer.parseInt(line.replaceFirst(N_H_LABEL, ""));

					//dat.imData = new Double[dcnt + dat.nh * dat.nw];
					/* Allocate space */
					dat.imData = new Double[nExtra + dat.nw * dat.nh];
//					dat.imData = new Double[dat.nh * dat.nw];
				} else {
					in.close();
					throw new IOException();
				}
			} else if (line_n == 3) {
				if (line.startsWith(NS_LABEL)) { /* Line: number of strokes */
					dat.nStrokes = Integer.parseInt(line.replaceFirst(NS_LABEL,
							""));

					//dat.imData[3] = (double) dat.nStrokes;
					if ( bIncludeTokenNumStrokes )
						dat.imData[dcnt++] = (double) dat.nStrokes;
						//extraFeatures.add((double) dat.nStrokes);
				} else {
					in.close();
					throw new IOException();
				}
			} else if (line_n == 4) {
				if (line.startsWith(W_LABEL)) {
					w = Double.parseDouble(line
							.replaceFirst(W_LABEL, ""));
					dat.w = (float) w;
					//dat.imData[0] = w;
					if ( bIncludeTokenSize )
						dat.imData[dcnt++] = w;
						//extraFeatures.add(w);
					
				} else {
					in.close();
					throw new IOException();
				}
			} else if (line_n == 5) {
				if (line.startsWith(H_LABEL)) {
					h = Double.parseDouble(line
							.replaceFirst(H_LABEL, ""));
					dat.h = (float) h;
					
					//dat.imData[1] = h;
					if ( bIncludeTokenSize )
						dat.imData[dcnt++] = h;
						//extraFeatures.add(h);
					
					//dat.imData[2] = dat.imData[0] / dat.imData[1];  /* whr: width-to-height ratio */
					if ( bIncludeTokenWHRatio ) {
						if ( w != -1.0 ) {
							//extraFeatures.add(w / h);
                            if (h == 0.0) {
                                dat.imData[dcnt++] = 100.0; // Prevent infinity values
                            } else {
                                dat.imData[dcnt++] = w / h;
                            }

						}
						else {
							System.err.println("w value has not been retrieved yet");
						}
					}
				} else {
					in.close();
					throw new IOException();
				}
			} else if (line_n >= 6) {
				String[] strs = line.split(" ");

				for (int i = 0; i < strs.length; ++i) {
					dat.imData[dcnt++] = Double.parseDouble(strs[i]);

                    if (dat.imData[dcnt - 1] < 0) {
                        throw new IllegalStateException("Encountered negative stroke image data");
                    }
				}
			}

			line_n++;
		}
		
		in.close();
		
//		if (dcnt != dat.imData.length) {
//			in.close();
//			throw new IOException();
//		}

		return dat;
	}
}
