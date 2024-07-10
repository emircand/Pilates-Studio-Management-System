import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { IQRCode, NewQRCode } from '../qr-code.model';

export type PartialUpdateQRCode = Partial<IQRCode> & Pick<IQRCode, 'id'>;

export type EntityResponseType = HttpResponse<IQRCode>;
export type EntityArrayResponseType = HttpResponse<IQRCode[]>;

@Injectable({ providedIn: 'root' })
export class QRCodeService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/qr-codes');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/qr-codes/_search');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(qRCode: NewQRCode): Observable<EntityResponseType> {
    return this.http.post<IQRCode>(this.resourceUrl, qRCode, { observe: 'response' });
  }

  update(qRCode: IQRCode): Observable<EntityResponseType> {
    return this.http.put<IQRCode>(`${this.resourceUrl}/${this.getQRCodeIdentifier(qRCode)}`, qRCode, { observe: 'response' });
  }

  partialUpdate(qRCode: PartialUpdateQRCode): Observable<EntityResponseType> {
    return this.http.patch<IQRCode>(`${this.resourceUrl}/${this.getQRCodeIdentifier(qRCode)}`, qRCode, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IQRCode>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IQRCode[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IQRCode[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(catchError(() => scheduled([new HttpResponse<IQRCode[]>()], asapScheduler)));
  }

  getQRCodeIdentifier(qRCode: Pick<IQRCode, 'id'>): number {
    return qRCode.id;
  }

  compareQRCode(o1: Pick<IQRCode, 'id'> | null, o2: Pick<IQRCode, 'id'> | null): boolean {
    return o1 && o2 ? this.getQRCodeIdentifier(o1) === this.getQRCodeIdentifier(o2) : o1 === o2;
  }

  addQRCodeToCollectionIfMissing<Type extends Pick<IQRCode, 'id'>>(
    qRCodeCollection: Type[],
    ...qRCodesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const qRCodes: Type[] = qRCodesToCheck.filter(isPresent);
    if (qRCodes.length > 0) {
      const qRCodeCollectionIdentifiers = qRCodeCollection.map(qRCodeItem => this.getQRCodeIdentifier(qRCodeItem)!);
      const qRCodesToAdd = qRCodes.filter(qRCodeItem => {
        const qRCodeIdentifier = this.getQRCodeIdentifier(qRCodeItem);
        if (qRCodeCollectionIdentifiers.includes(qRCodeIdentifier)) {
          return false;
        }
        qRCodeCollectionIdentifiers.push(qRCodeIdentifier);
        return true;
      });
      return [...qRCodesToAdd, ...qRCodeCollection];
    }
    return qRCodeCollection;
  }
}
