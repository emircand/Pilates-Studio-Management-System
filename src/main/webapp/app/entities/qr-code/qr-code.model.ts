export interface IQRCode {
  id: number;
  code?: string | null;
  sessionId?: string | null;
  athleteId?: string | null;
  coachId?: string | null;
}

export type NewQRCode = Omit<IQRCode, 'id'> & { id: null };
